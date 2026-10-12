package com.example.chat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PresenceServiceTest {

    private PresenceService presenceService;

    @BeforeEach
    void setUp() {
        presenceService = new PresenceService();
    }

    @Test
    void registerAddsUser() {
        presenceService.register("alice");

        assertThat(presenceService.online()).containsExactly("alice");
        assertThat(presenceService.count()).isEqualTo(1);
    }

    @Test
    void registerIsIdempotent() {
        presenceService.register("alice");
        presenceService.register("alice");

        assertThat(presenceService.count()).isEqualTo(1);
    }

    @Test
    void registerTrimsWhitespace() {
        presenceService.register("  bob  ");

        assertThat(presenceService.online()).containsExactly("bob");
    }

    @Test
    void registerIgnoresNullOrBlank() {
        presenceService.register(null);
        presenceService.register("   ");

        assertThat(presenceService.online()).isEmpty();
        assertThat(presenceService.count()).isZero();
    }

    @Test
    void removeDeletesUser() {
        presenceService.register("alice");
        presenceService.register("bob");

        presenceService.remove("alice");

        assertThat(presenceService.online()).containsExactly("bob");
    }

    @Test
    void onlineIsSortedAndUnmodifiable() {
        presenceService.register("charlie");
        presenceService.register("alice");
        presenceService.register("bob");

        assertThat(presenceService.online()).containsExactly("alice", "bob", "charlie");
        assertThat(presenceService.online()).isUnmodifiable();
    }
}
