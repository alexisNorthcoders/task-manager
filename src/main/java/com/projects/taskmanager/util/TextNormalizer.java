package com.projects.taskmanager.util;

import org.springframework.stereotype.Component;

@Component
public class TextNormalizer {
    public String normalizeTitle(String title) {
        if (title == null) {
            return null;
        }
        String trimmed = title.trim();
        return trimmed.replaceAll("\\s+", " ");
    }
}


