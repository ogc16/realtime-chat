# Realtime Chat

A realtime chat application built with **Spring Boot**, **Spring WebSocket (STOMP)**, and a **SockJS** web client.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-green)
![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-blue)

## Features

- Realtime messaging over WebSocket with STOMP protocol
- SockJS fallback (auto-negotiates WebSocket → HTTP long-polling when WebSocket is unavailable)
- Join / leave notifications with a live "online users" list
- Clean responsive web UI (dark theme), works on desktop and mobile
- Maven wrapper included — no global Maven install needed

## Tech Stack

| Layer     | Technology                          |
|-----------|-------------------------------------|
| Backend   | Java 21, Spring Boot 3.5            |
| Messaging | Spring WebSocket + STOMP            |
| Frontend  | HTML/CSS/JS, SockJS, `@stomp/stompjs` |

## Project Structure

```
chat/
├── pom.xml                                  # Maven build config
├── mvnw, mvnw.cmd                           # Maven wrapper scripts
├── .mvn/wrapper/maven-wrapper.properties
└── src/main/
    ├── java/com/example/chat/
    │   ├── ChatApplication.java             # Spring Boot entry point
    │   ├── config/WebSocketConfig.java      # STOMP endpoint + broker config
    │   ├── controller/ChatController.java   # MessageMapping handlers
    │   └── model/ChatMessage.java           # Chat payload (CHAT / JOIN / LEAVE)
    └── resources/
        ├── application.properties           # server.port=8080
        └── static/
            ├── index.html                   # Web chat client
            └── vendor/                      # SockJS + stomp.js (vendored locally)
```

## Prerequisites

- [JDK 21](https://adoptium.net/) (or newer)
- Nothing else — Maven is bundled via the wrapper

## How to Run

```bash
# Build
./mvnw clean package          # Windows: mvnw.cmd clean package

# Run
./mvnw spring-boot:run        # or: java -jar target/chat-0.0.1-SNAPSHOT.jar
```

Then open **http://localhost:8080** in two browser tabs, enter a name, and start chatting.

To change the port, edit `server.port` in `src/main/resources/application.properties` and restart.

## How It Works

1. The browser connects to `/ws` using SockJS and establishes a STOMP session.
2. The client subscribes to the broadcast topic `/topic/public`.
3. Messages sent to `/app/chat.sendMessage` are handled by `ChatController.sendMessage()` and broadcast to all subscribers via `/topic/public` (`@SendTo`).
4. Join events go to `/app/chat.addUser`; the client tracks the resulting JOIN/LEAVE frames to maintain the online-user list.

```
Browser ──STOMP──▶ /app/chat.sendMessage   (client → server)
   ▲                                      ChatController
   │                                            │
   └────────── /topic/public ◀─────────────────┘   (server → all clients)
```

### WebSocket Config (`WebSocketConfig.java`)

| Setting                    | Value                | Purpose                              |
|----------------------------|----------------------|--------------------------------------|
| Simple broker destinations | `/topic`             | Server-to-client broadcasts          |
| Application prefix         | `/app`               | Client-to-server `@MessageMapping`   |
| SockJS endpoint            | `/ws`                | WebSocket handshake endpoint         |

## Extending

- **Private/group chats**: add destinations like `/user` with `convertAndSendToUser` and a destination prefix (`/user`).
- **Persistence**: add `spring-boot-starter-data-jpa` + a DB, and a listener on the broker to store messages.
- **External broker**: swap the simple broker for RabbitMQ (`spring-boot-starter-amqp`) or ActiveMQ for multi-instance scaling.
- **Auth**: protect the endpoint with Spring Security and resolve the sender from the STOMP `CONNECT` frame headers.

## Testing

```bash
./mvnw test
```

The SockJS info endpoint can be probed to confirm WebSocket support:

```bash
curl http://localhost:8080/ws/info   # → {"websocket":true, ...}
```
