package com.reviewtopper.util;

import lombok.experimental.UtilityClass;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@UtilityClass
public class SlugGenerator {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern HYPHENS = Pattern.compile("-{2,}");

    public static String fromName(String name) {
        String normalized = Normalizer.normalize(name.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String lower = normalized.toLowerCase(Locale.ROOT);
        String replacedSpaces = lower.replace(' ', '-');
        String slug = NON_LATIN.matcher(replacedSpaces).replaceAll("");
        slug = HYPHENS.matcher(slug).replaceAll("-");
        slug = slug.replaceAll("^-+|+-+$", "");
        return slug.length() > 140 ? slug.substring(0, 140) : slug;
    }
}
