package com.reviewtopper.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.reviewtopper.config.ReviewTopperProperties;
import com.reviewtopper.entity.Workspace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrCodeService {

    private final ReviewTopperProperties properties;

    public byte[] generateLandingPageQrPng(Workspace workspace, int pixelSize) {
        String rawBase = properties.getPublicUrls().getFrontendBaseUrl();
        if (rawBase == null || rawBase.isBlank()) {
            rawBase = "http://localhost:5173";
            log.warn("review-topper.public-urls.frontend-base-url is blank; falling back to {}", rawBase);
        }
        String base = rawBase.replaceAll("/+$", "");
        String payload = base + "/r/" + workspace.getSlug();
        String hex = workspace.getThemeConfig() != null && workspace.getThemeConfig().getPrimaryColor() != null
                ? workspace.getThemeConfig().getPrimaryColor()
                : "#111827";
        int onColor = parseRgb(hex, 0xFF111827);
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix matrix = writer.encode(payload, BarcodeFormat.QR_CODE, pixelSize, pixelSize, hints);
            MatrixToImageConfig config = new MatrixToImageConfig(onColor, MatrixToImageConfig.WHITE);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix, config);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            byte[] png = baos.toByteArray();
            log.debug(
                    "QR PNG for slug={}: payloadChars={}, pngBytes={}",
                    workspace.getSlug(),
                    payload.length(),
                    png.length);
            return png;
        } catch (WriterException e) {
            throw new IllegalStateException("QR generation failed", e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static int parseRgb(String hex, int fallbackArgb) {
        if (hex == null || hex.isBlank()) {
            return fallbackArgb;
        }
        String h = hex.trim();
        if (h.startsWith("#")) {
            h = h.substring(1);
        }
        try {
            if (h.length() == 6) {
                int rgb = Integer.parseUnsignedInt(h, 16);
                return (0xFF << 24) | rgb;
            }
            if (h.length() == 3) {
                int r = Integer.parseUnsignedInt(h.substring(0, 1), 16) * 17;
                int g = Integer.parseUnsignedInt(h.substring(1, 2), 16) * 17;
                int b = Integer.parseUnsignedInt(h.substring(2, 3), 16) * 17;
                return (0xFF << 24) | (r << 16) | (g << 8) | b;
            }
        } catch (NumberFormatException ignored) {
            return fallbackArgb;
        }
        return fallbackArgb;
    }
}
