package com.example.chat.controller;

import com.example.chat.service.AutocompleteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/autocomplete")
public class AutocompleteController {

    private final AutocompleteService autocompleteService;

    public AutocompleteController(AutocompleteService autocompleteService) {
        this.autocompleteService = autocompleteService;
    }

    @GetMapping
    public List<String> suggestions(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "limit", required = false) Integer limit) {
        return autocompleteService.suggest(query, limit);
    }
}
