package com.example.chat.controller;

import com.example.chat.model.PresenceSnapshot;
import com.example.chat.service.PresenceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/presence")
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @GetMapping
    public PresenceSnapshot presence() {
        Set<String> users = presenceService.online();
        return new PresenceSnapshot(users, users.size());
    }
}
