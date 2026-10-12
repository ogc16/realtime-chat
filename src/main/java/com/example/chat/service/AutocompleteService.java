package com.example.chat.service;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Learns the words used in the room and returns typeahead completions.
 * Purely in-memory and process-local; no external services are involved.
 */
@Service
public class AutocompleteService {

    private static final Pattern WORD = Pattern.compile("[A-Za-z][A-Za-z0-9']*");
    private static final int DEFAULT_LIMIT = 8;
    private static final int MAX_LIMIT = 20;

    private static final List<String> SEED_WORDS = List.of(
            "hello", "hi", "hey", "thanks", "please", "yes", "no", "okay",
            "sure", "maybe", "welcome", "good", "morning", "afternoon", "evening",
            "how", "are", "you", "what", "when", "where", "why", "who",
            "the", "and", "for", "with", "chat", "message", "online", "join", "leave"
    );

    private final Map<String, Integer> frequency = new ConcurrentHashMap<>();
    private final Map<String, String> display = new ConcurrentHashMap<>();

    public AutocompleteService() {
        SEED_WORDS.forEach(word -> {
            frequency.put(word, 1);
            display.put(word, word);
        });
    }

    /** Extracts words from a chat message and adds them to the dictionary. */
    public void record(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        Matcher matcher = WORD.matcher(message);
        while (matcher.find()) {
            String word = matcher.group();
            String key = word.toLowerCase(Locale.ROOT);
            display.putIfAbsent(key, word);
            frequency.merge(key, 1, Integer::sum);
        }
    }

    /** Returns up to {@code limit} known words starting with {@code prefix}, most frequent first. */
    public List<String> suggest(String prefix, Integer limit) {
        if (prefix == null || prefix.isBlank()) {
            return List.of();
        }
        String normalized = prefix.toLowerCase(Locale.ROOT);
        int max = normalizeLimit(limit);
        Set<String> words = frequency.keySet();
        return words.stream()
                .filter(word -> word.startsWith(normalized) && !word.equals(normalized))
                .sorted(Comparator
                        .comparingInt((String word) -> frequency.getOrDefault(word, 0)).reversed()
                        .thenComparing(Comparator.naturalOrder()))
                .limit(max)
                .map(display::get)
                .toList();
    }

    public int size() {
        return frequency.size();
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
