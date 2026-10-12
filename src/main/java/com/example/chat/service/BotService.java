package com.example.chat.service;

import com.example.chat.config.RagProperties;
import com.example.chat.model.ChatMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Turns a chat message into a RAG-backed bot answer when it starts with the
 * configured command (default {@code /ask}) and broadcasts the reply.
 */
@Service
public class BotService {

    private final RagClient ragClient;
    private final RagProperties properties;
    private final SimpMessagingTemplate messagingTemplate;

    public BotService(RagClient ragClient, RagProperties properties, SimpMessagingTemplate messagingTemplate) {
        this.ragClient = ragClient;
        this.properties = properties;
        this.messagingTemplate = messagingTemplate;
    }

    public void maybeReply(ChatMessage message) {
        if (message.getType() != ChatMessage.MessageType.CHAT) {
            return;
        }
        commandQuestion(message.getContent()).flatMap(ragClient::ask).ifPresent(this::broadcast);
    }

    private Optional<String> commandQuestion(String content) {
        String command = properties.getCommand();
        if (content == null || command == null || command.isBlank()) {
            return Optional.empty();
        }
        String trimmed = content.trim();
        if (!trimmed.toLowerCase().startsWith(command.toLowerCase())) {
            return Optional.empty();
        }
        String question = trimmed.substring(command.length()).trim();
        return question.isEmpty() ? Optional.empty() : Optional.of(question);
    }

    private void broadcast(String answer) {
        ChatMessage reply = new ChatMessage(ChatMessage.MessageType.CHAT, answer, properties.getBotName());
        messagingTemplate.convertAndSend("/topic/public", reply);
    }
}
