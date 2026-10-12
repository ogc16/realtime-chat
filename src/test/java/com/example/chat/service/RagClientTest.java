package com.example.chat.service;

import com.example.chat.config.RagProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RagClientTest {

    private RagProperties properties;
    private MockRestServiceServer server;
    private RagClient ragClient;

    @BeforeEach
    void setUp() {
        properties = new RagProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("http://rag.test");
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        ragClient = new RagClient(properties, builder);
    }

    @Test
    void returnsAnswerFromSidecar() {
        server.expect(requestTo("http://rag.test/query"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"answer\":\"42\"}", MediaType.APPLICATION_JSON));

        assertThat(ragClient.ask("what is 2+2")).contains("42");
        server.verify();
    }

    @Test
    void disabledReturnsEmptyWithoutCalling() {
        properties.setEnabled(false);

        assertThat(ragClient.ask("anything")).isEmpty();
    }

    @Test
    void blankQuestionReturnsEmpty() {
        assertThat(ragClient.ask("   ")).isEmpty();
    }

    @Test
    void serverErrorReturnsEmpty() {
        server.expect(requestTo("http://rag.test/query"))
                .andRespond(withServerError());

        assertThat(ragClient.ask("boom")).isEmpty();
    }
}
