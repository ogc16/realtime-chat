package com.example.chat.service;

import com.example.chat.model.ChatMessage;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
public class ChatService {

    private final PresenceService presenceService;
    private final AutocompleteService autocompleteService;

    public ChatService(PresenceService presenceService, AutocompleteService autocompleteService) {
        this.presenceService = presenceService;
        this.autocompleteService = autocompleteService;
    }

    public ChatMessage handleChat(ChatMessage message) {
        if (message.getType() == null) {
            message.setType(ChatMessage.MessageType.CHAT);
        }
        message.setContent(message.getContent() == null ? "" : message.getContent().trim());
        autocompleteService.record(message.getContent());
        stamp(message);
        return message;
    }

    public ChatMessage handleJoin(ChatMessage message) {
        String sender = message.getSender() == null ? "" : message.getSender().trim();
        message.setSender(sender);
        message.setType(ChatMessage.MessageType.JOIN);
        message.setContent("");
        stamp(message);
        presenceService.register(sender);
        return message;
    }

    public ChatMessage handleLeave(ChatMessage message) {
        String sender = message.getSender() == null ? "" : message.getSender().trim();
        message.setSender(sender);
        message.setType(ChatMessage.MessageType.LEAVE);
        message.setContent("");
        stamp(message);
        presenceService.remove(sender);
        return message;
    }

    public Set<String> onlineUsers() {
        return presenceService.online();
    }

    private void stamp(ChatMessage message) {
        if (message.getTimestamp() <= 0) {
            message.setTimestamp(Instant.now().toEpochMilli());
        }
    }
}
