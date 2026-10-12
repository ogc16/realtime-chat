package com.example.chat.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {

    private final Set<String> users = ConcurrentHashMap.newKeySet();

    public void register(String sender) {
        String name = normalize(sender);
        if (name != null) {
            users.add(name);
        }
    }

    public void remove(String sender) {
        String name = normalize(sender);
        if (name != null) {
            users.remove(name);
        }
    }

    public Set<String> online() {
        return Collections.unmodifiableSet(new TreeSet<>(users));
    }

    public int count() {
        return users.size();
    }

    private String normalize(String sender) {
        if (sender == null) {
            return null;
        }
        String trimmed = sender.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
