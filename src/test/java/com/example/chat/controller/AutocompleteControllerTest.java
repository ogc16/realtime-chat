package com.example.chat.controller;

import com.example.chat.service.AutocompleteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AutocompleteController.class)
class AutocompleteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AutocompleteService autocompleteService;

    @Test
    void returnsSuggestions() throws Exception {
        given(autocompleteService.suggest(eq("hel"), any())).willReturn(List.of("hello", "help"));

        mockMvc.perform(get("/api/autocomplete").param("q", "hel").param("limit", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("hello"))
                .andExpect(jsonPath("$[1]").value("help"));
    }

    @Test
    void returnsEmptyListWithoutQuery() throws Exception {
        given(autocompleteService.suggest(any(), any())).willReturn(List.of());

        mockMvc.perform(get("/api/autocomplete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
