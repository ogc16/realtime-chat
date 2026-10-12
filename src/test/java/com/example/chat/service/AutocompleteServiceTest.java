package com.example.chat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AutocompleteServiceTest {

    private AutocompleteService autocompleteService;

    @BeforeEach
    void setUp() {
        autocompleteService = new AutocompleteService();
    }

    @Test
    void suggestsSeededWordsByPrefix() {
        assertThat(autocompleteService.suggest("hel", 6)).contains("hello");
    }

    @Test
    void learnsWordsFromMessages() {
        autocompleteService.record("pineapples are delicious, pineapples!");

        assertThat(autocompleteService.suggest("pine", 6)).containsExactly("pineapples");
    }

    @Test
    void excludesTheExactTypedWord() {
        assertThat(autocompleteService.suggest("hello", 6)).doesNotContain("hello");
    }

    @Test
    void ordersByFrequencyThenAlphabetically() {
        autocompleteService.record("banana banana grape");
        autocompleteService.record("grapefruit");
        autocompleteService.record("banana");

        assertThat(autocompleteService.suggest("ba", 5)).containsExactly("banana");
        assertThat(autocompleteService.suggest("gr", 5)).containsExactly("grape", "grapefruit");
    }

    @Test
    void respectsLimit() {
        autocompleteService.record("aa ab ac ad");

        assertThat(autocompleteService.suggest("a", 2)).hasSize(2);
    }

    @Test
    void ignoresNullBlankAndEmptyPrefix() {
        assertThat(autocompleteService.suggest(null, 5)).isEmpty();
        assertThat(autocompleteService.suggest("  ", 5)).isEmpty();
        autocompleteService.record(null);
        autocompleteService.record("   ");
        assertThat(autocompleteService.size()).isPositive();
    }

    @Test
    void prefixMatchIsCaseInsensitiveButPreservesDisplayCase() {
        autocompleteService.record("Kubernetes");

        assertThat(autocompleteService.suggest("kub", 5)).containsExactly("Kubernetes");
    }
}
