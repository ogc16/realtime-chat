package com.example.chat.controller;

import com.example.chat.service.PresenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.TreeSet;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PresenceController.class)
class PresenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PresenceService presenceService;

    @Test
    void returnsOnlineUsersAndCount() throws Exception {
        given(presenceService.online()).willReturn(new TreeSet<>(Set.of("alice", "bob")));

        mockMvc.perform(get("/api/presence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.users[0]").value("alice"))
                .andExpect(jsonPath("$.users[1]").value("bob"));
    }

    @Test
    void returnsEmptyWhenNobodyOnline() throws Exception {
        given(presenceService.online()).willReturn(Set.of());

        mockMvc.perform(get("/api/presence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0))
                .andExpect(jsonPath("$.users").isEmpty());
    }
}
