package com.example.chat;

import com.example.chat.model.ChatMessage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatWebSocketIT {

    @LocalServerPort
    private int port;

    @Test
    void broadcastsChatMessageToSubscribers() throws Exception {
        String url = "http://localhost:" + port + "/ws";
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(transports));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        BlockingQueue<ChatMessage> received = new LinkedBlockingQueue<>();

        StompSession session = stompClient
                .connectAsync(url, new StompSessionHandlerAdapter() {
                })
                .get(5, TimeUnit.SECONDS);

        session.subscribe("/topic/public", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                received.add((ChatMessage) payload);
            }
        });

        ChatMessage outgoing = new ChatMessage(ChatMessage.MessageType.CHAT, "hello world", "alice");
        session.send("/app/chat.sendMessage", outgoing);

        ChatMessage result = received.poll(5, TimeUnit.SECONDS);
        session.disconnect();

        assertThat(result).isNotNull();
        assertThat(result.getSender()).isEqualTo("alice");
        assertThat(result.getContent()).isEqualTo("hello world");
        assertThat(result.getType()).isEqualTo(ChatMessage.MessageType.CHAT);
    }
}
