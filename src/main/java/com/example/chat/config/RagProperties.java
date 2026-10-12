package com.example.chat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the optional RAG sidecar (see {@code rag-service/}).
 */
@ConfigurationProperties(prefix = "rag")
public class RagProperties {

    /** Whether the bot integration is active. Disabled by default. */
    private boolean enabled = false;

    /** Base URL of the RAG sidecar's HTTP API. */
    private String baseUrl = "http://localhost:8000";

    /** Message prefix that triggers a bot answer, e.g. {@code /ask}. */
    private String command = "/ask";

    /** Display name broadcast for bot answers. */
    private String botName = "Assistant";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }
}
