package com.example.chat.service;

import com.example.chat.config.RagProperties;
import com.example.chat.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class BotServiceTest {

    @Mock
    private RagClient ragClient;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private BotService botService;

    @BeforeEach
    void setUp() {
        RagProperties properties = new RagProperties();
        properties.setCommand("/ask");
        properties.setBotName("Assistant");
        botService = new BotService(ragClient, properties, messagingTemplate);
    }

    @Test
    void broadcastsAnswerForCommand() {
        given(ragClient.ask("what is 2+2")).willReturn(Optional.of("4"));

        botService.maybeReply(new ChatMessage(ChatMessage.MessageType.CHAT, "/ask what is 2+2", "alice"));

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/public"), captor.capture());
        assertThat(captor.getValue().getSender()).isEqualTo("Assistant");
        assertThat(captor.getValue().getContent()).isEqualTo("4");
        assertThat(captor.getValue().getType()).isEqualTo(ChatMessage.MessageType.CHAT);
    }

    @Test
    void commandMatchIsCaseInsensitive() {
        given(ragClient.ask("hi")).willReturn(Optional.of("hello"));

        botService.maybeReply(new ChatMessage(ChatMessage.MessageType.CHAT, "/ASK hi", "alice"));

        verify(messagingTemplate).convertAndSend(eq("/topic/public"), org.mockito.ArgumentMatchers.any(ChatMessage.class));
    }

    @Test
    void ignoresNonCommandMessages() {
        botService.maybeReply(new ChatMessage(ChatMessage.MessageType.CHAT, "just chatting", "alice"));

        verifyNoInteractions(ragClient);
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void ignoresNonChatTypes() {
        botService.maybeReply(new ChatMessage(ChatMessage.MessageType.JOIN, "/ask hi", "alice"));

        verifyNoInteractions(ragClient);
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void ignoresCommandWithoutQuestion() {
        botService.maybeReply(new ChatMessage(ChatMessage.MessageType.CHAT, "/ask", "alice"));

        verifyNoInteractions(ragClient);
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void doesNothingWhenNoAnswerAvailable() {
        given(ragClient.ask("unknown")).willReturn(Optional.empty());

        botService.maybeReply(new ChatMessage(ChatMessage.MessageType.CHAT, "/ask unknown", "alice"));

        verifyNoInteractions(messagingTemplate);
    }
}
