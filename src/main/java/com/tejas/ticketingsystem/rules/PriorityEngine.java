package com.tejas.ticketingsystem.rules;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.tejas.ticketingsystem.entity.Category;
import com.tejas.ticketingsystem.enums.Priority;

@Component
public class PriorityEngine {
    // Keywords that signal HIGH priority, per category
    private static final Map<String, List<String>> HIGH_PRIORITY_KEYWORDS = Map.of(
            "ACCOUNT", List.of("locked", "login", "can't access", "unauthorized", "suspended"),
            "BILLING", List.of("charged", "refund", "duplicate", "overcharged", "payment failed"),
            "TECHNICAL", List.of("crash", "down", "not working", "error", "broken", "500"),
            "GENERAL", List.of("urgent", "critical", "immediately", "asap")
    );

    public Priority determinePriority(Category category, String description) {
        String categoryName = category.getName().toUpperCase();
        String lowerDescription = description.toLowerCase();

        List<String> keywords = HIGH_PRIORITY_KEYWORDS.getOrDefault(categoryName, List.of());

        for (String keyword : keywords) {
            if (lowerDescription.contains(keyword)) {
                return Priority.HIGH;
            }
        }

        return Priority.MEDIUM;
    }
}
