package com.example.chat.model;

import java.util.Set;

public record PresenceSnapshot(Set<String> users, int count) {
}
