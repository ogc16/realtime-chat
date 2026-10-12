package com.example.chat.controller;

import com.example.chat.model.ChatMessage;
import com.example.chat.service.BotService;
import com.example.chat.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final ChatService chatService;
    private final BotService botService;

    public ChatController(ChatService chatService, BotService botService) {
        this.chatService = chatService;
        this.botService = botService;
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage message) {
        ChatMessage processed = chatService.handleChat(message);
        botService.maybeReply(processed);
        return processed;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage message) {
        return chatService.handleJoin(message);
    }

    @MessageMapping("/chat.leaveUser")
    @SendTo("/topic/public")
    public ChatMessage leaveUser(@Payload ChatMessage message) {
        return chatService.handleLeave(message);
    }
}
