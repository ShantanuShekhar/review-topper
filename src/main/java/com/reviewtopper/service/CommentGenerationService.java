package com.reviewtopper.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reviewtopper.config.ReviewTopperProperties;
import com.reviewtopper.enums.FeedbackLanguage;
import com.reviewtopper.enums.Sentiment;
import com.reviewtopper.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentGenerationService {

    private final ObjectProvider<ChatModel> chatModelProvider;
    private final WorkspaceRepository workspaceRepository;
    private final ReviewTopperProperties properties;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.api-key:}")
    private String openAiApiKey;

    private final ExecutorService aiExecutor = Executors.newCachedThreadPool();

    public List<String> generate(Sentiment sentiment, FeedbackLanguage language, String workspaceSlug) {
        String businessName = resolveBusinessName(workspaceSlug);
        if (properties.getAi().isEnabled() && hasOpenAiKey()) {
            try {
                return aiExecutor
                        .submit(() -> generateWithAi(sentiment, language, businessName))
                        .get(properties.getAi().getTimeoutMs(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                log.warn("AI comment generation timed out after {}ms", properties.getAi().getTimeoutMs());
            } catch (Exception e) {
                log.warn("AI comment generation failed: {}", e.getMessage());
            }
        }
        return fallbackComments(sentiment, language);
    }

    private boolean hasOpenAiKey() {
        return openAiApiKey != null && !openAiApiKey.isBlank();
    }

    private String resolveBusinessName(String workspaceSlug) {
        if (workspaceSlug == null || workspaceSlug.isBlank()) {
            return "the business";
        }
        return workspaceRepository
                .findBySlugIgnoreCaseAndActiveTrue(workspaceSlug.trim())
                .map(w -> w.getName())
                .orElse("the business");
    }

    private List<String> generateWithAi(Sentiment sentiment, FeedbackLanguage language, String businessName) {
        ChatModel chatModel = chatModelProvider.getIfAvailable();
        if (chatModel == null) {
            return fallbackComments(sentiment, language);
        }
        String tone = switch (sentiment) {
            case POSITIVE -> "positive and appreciative";
            case NEGATIVE -> "constructive but disappointed";
            case NEUTRAL -> "balanced and average";
        };
        String langLabel = switch (language) {
            case HINGLISH -> "Hinglish (mix of Hindi and English, casual Indian tone)";
            case HINDI -> "Hindi (Devanagari script)";
            case ENGLISH -> "English";
        };
        String prompt =
                """
                You write short customer feedback for "%s".
                Tone: %s.
                Language: %s.
                Return ONLY a JSON array of exactly 3 strings. Each string is ONE short sentence (max 14 words), no numbering, no markdown.
                """
                        .formatted(businessName, tone, langLabel);
        String raw = chatModel.call(new Prompt(prompt)).getResult().getOutput().getText();
        return parseComments(raw, sentiment, language);
    }

    private List<String> parseComments(String raw, Sentiment sentiment, FeedbackLanguage language) {
        if (raw == null || raw.isBlank()) {
            return fallbackComments(sentiment, language);
        }
        String trimmed = raw.trim();
        int start = trimmed.indexOf('[');
        int end = trimmed.lastIndexOf(']');
        if (start >= 0 && end > start) {
            try {
                List<String> parsed =
                        objectMapper.readValue(trimmed.substring(start, end + 1), new TypeReference<>() {});
                return normalizeThree(parsed, sentiment, language);
            } catch (Exception ignored) {
                // fall through
            }
        }
        String[] lines = trimmed.split("\\r?\\n");
        List<String> fromLines = new ArrayList<>();
        for (String line : lines) {
            String s = line.replaceAll("^[-*\\d.\\s]+", "").trim();
            if (!s.isBlank()) {
                fromLines.add(s);
            }
        }
        return normalizeThree(fromLines, sentiment, language);
    }

    private List<String> normalizeThree(List<String> input, Sentiment sentiment, FeedbackLanguage language) {
        List<String> result = new ArrayList<>();
        for (String s : input) {
            if (s == null || s.isBlank()) {
                continue;
            }
            String t = s.trim();
            result.add(t.length() > 160 ? t.substring(0, 157) + "…" : t);
            if (result.size() == 3) {
                return result;
            }
        }
        for (String s : fallbackComments(sentiment, language)) {
            if (result.size() >= 3) {
                break;
            }
            if (!result.contains(s)) {
                result.add(s);
            }
        }
        return result.subList(0, Math.min(3, result.size()));
    }

    public List<String> fallbackComments(Sentiment sentiment, FeedbackLanguage language) {
        return switch (language) {
            case HINDI -> hindiFallback(sentiment);
            case HINGLISH -> hinglishFallback(sentiment);
            case ENGLISH -> englishFallback(sentiment);
        };
    }

    private List<String> englishFallback(Sentiment sentiment) {
        return switch (sentiment) {
            case POSITIVE -> List.of(
                    "Wonderful experience — friendly staff and great service!",
                    "Everything exceeded my expectations today, highly recommend.",
                    "Clean, professional, and genuinely caring team. Will visit again.");
            case NEGATIVE -> List.of(
                    "Service was slow and my issue was not resolved properly.",
                    "Expected much better quality for the price I paid.",
                    "Staff seemed uninterested and the experience felt disappointing.");
            case NEUTRAL -> List.of(
                    "Decent visit overall — a few things could be smoother.",
                    "Average experience; nothing stood out but nothing terrible either.",
                    "Okay service, might return if small improvements are made.");
        };
    }

    private List<String> hinglishFallback(Sentiment sentiment) {
        return switch (sentiment) {
            case POSITIVE -> List.of(
                    "Bahut accha experience tha, staff bohot helpful thi!",
                    "Service top-notch thi, definitely dubara aaunga.",
                    "Sab kuch smooth tha — highly recommend karunga friends ko.");
            case NEGATIVE -> List.of(
                    "Service slow thi aur problem properly solve nahi hui.",
                    "Price ke hisaab se quality disappointing thi.",
                    "Staff ka attitude thoda off tha, experience achha nahi laga.");
            case NEUTRAL -> List.of(
                    "Theek tha overall, kuch cheezein aur better ho sakti hain.",
                    "Average experience — na kharab na zyada khaas.",
                    "Chalega, agar thodi improvement ho to phir try karunga.");
        };
    }

    private List<String> hindiFallback(Sentiment sentiment) {
        return switch (sentiment) {
            case POSITIVE -> List.of(
                    "अनुभव बहुत अच्छा रहा, कर्मचारी विनम्र और सहायक थे।",
                    "सेवा उत्कृष्ट थी, मैं निश्चित रूप से दोबारा आऊँगा।",
                    "सब कुछ सुव्यवस्थित था, मैं दोस्तों को अवश्य सुझाऊँगा।");
            case NEGATIVE -> List.of(
                    "सेवा धीमी थी और समस्या ठीक से हल नहीं हुई।",
                    "कीमत के मुकाबले गुणवत्ता निराशाजनक थी।",
                    "कर्मचारियों का व्यवहार उदासीन लगा, अनुभव खराब रहा।");
            case NEUTRAL -> List.of(
                    "कुल मिलाकर ठीक था, कुछ सुधार की गुंजाइश है।",
                    "औसत अनुभव — न अच्छा न बुरा।",
                    "चलेगा, सुधार होने पर फिर कोशिश करूँगा।");
        };
    }
}
