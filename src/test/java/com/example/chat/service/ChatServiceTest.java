package com.example.chat.service;

import com.example.chat.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatServiceTest {

    private PresenceService presenceService;
    private AutocompleteService autocompleteService;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        presenceService = new PresenceService();
        autocompleteService = new AutocompleteService();
        chatService = new ChatService(presenceService, autocompleteService);
    }

    private ChatMessage blankMessage() {
        return new ChatMessage();
    }

    @Test
    void handleChatKeepsProvidedType() {
        ChatMessage message = new ChatMessage(ChatMessage.MessageType.CHAT, "hello", "alice");

        ChatMessage result = chatService.handleChat(message);

        assertThat(result.getType()).isEqualTo(ChatMessage.MessageType.CHAT);
        assertThat(result.getContent()).isEqualTo("hello");
        assertThat(result.getTimestamp()).isPositive();
    }

    @Test
    void handleChatDefaultsMissingTypeToChat() {
        ChatMessage message = blankMessage();
        message.setContent("hi");
        message.setSender("alice");

        ChatMessage result = chatService.handleChat(message);

        assertThat(result.getType()).isEqualTo(ChatMessage.MessageType.CHAT);
    }

    @Test
    void handleChatTrimsContentAndDefaultsNullToEmpty() {
        ChatMessage message = blankMessage();
        message.setContent("   spaced   ");

        assertThat(chatService.handleChat(message).getContent()).isEqualTo("spaced");

        message.setContent(null);
        assertThat(chatService.handleChat(message).getContent()).isEmpty();
    }

    @Test
    void handleChatStampsTimestampWhenMissing() {
        ChatMessage message = blankMessage();
        message.setTimestamp(0);

        ChatMessage result = chatService.handleChat(message);

        assertThat(result.getTimestamp()).isPositive();
    }

    @Test
    void handleChatPreservesClientTimestamp() {
        ChatMessage message = blankMessage();
        message.setTimestamp(123456789L);

        assertThat(chatService.handleChat(message).getTimestamp()).isEqualTo(123456789L);
    }

    @Test
    void handleJoinRegistersPresenceAndNormalizes() {
        ChatMessage message = blankMessage();
        message.setType(ChatMessage.MessageType.JOIN);
        message.setSender("  alice  ");
        message.setContent("ignored");

        ChatMessage result = chatService.handleJoin(message);

        assertThat(result.getType()).isEqualTo(ChatMessage.MessageType.JOIN);
        assertThat(result.getSender()).isEqualTo("alice");
        assertThat(result.getContent()).isEmpty();
        assertThat(chatService.onlineUsers()).containsExactly("alice");
    }

    @Test
    void handleChatFeedsAutocomplete() {
        chatService.handleChat(new ChatMessage(ChatMessage.MessageType.CHAT, "pineapple", "alice"));

        assertThat(autocompleteService.suggest("pine", 6)).contains("pineapple");
    }

    @Test
    void handleLeaveRemovesPresence() {
        presenceService.register("alice");

        ChatMessage message = blankMessage();
        message.setType(ChatMessage.MessageType.LEAVE);
        message.setSender("alice");

        ChatMessage result = chatService.handleLeave(message);

        assertThat(result.getType()).isEqualTo(ChatMessage.MessageType.LEAVE);
        assertThat(chatService.onlineUsers()).isEmpty();
    }
}
