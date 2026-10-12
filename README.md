# Realtime Chat

A realtime chat application built with **Spring Boot**, **Spring WebSocket (STOMP)**, and a **SockJS** web client.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-green)
![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-blue)
[![CI](https://github.com/ogc16/realtime-chat/actions/workflows/ci.yml/badge.svg)](https://github.com/ogc16/realtime-chat/actions/workflows/ci.yml)
[![CodeQL](https://github.com/ogc16/realtime-chat/actions/workflows/codeql.yml/badge.svg)](https://github.com/ogc16/realtime-chat/actions/workflows/codeql.yml)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/ogc16/realtime-chat/badge)](https://securityscorecards.dev/viewer/?uri=github.com/ogc16/realtime-chat)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## Features

- Realtime messaging over WebSocket with STOMP protocol
- SockJS fallback (auto-negotiates WebSocket → HTTP long-polling when WebSocket is unavailable)
- Join / leave notifications with a live "online users" list
- **Message autocomplete / typeahead** in the composer (learns words from the conversation)
- Optional **RAG bot** — type `/ask <question>` to get an answer grounded in your docs (see [`rag-service/`](rag-service/))
- Clean responsive web UI (dark theme), works on desktop and mobile
- Maven wrapper included — no global Maven install needed

## Tech Stack

| Layer      | Technology                             |
|------------|----------------------------------------|
| Backend    | Java 21, Spring Boot 3.5               |
| Messaging  | Spring WebSocket + STOMP               |
| Frontend   | HTML/CSS/JS, SockJS, `@stomp/stompjs`  |
| RAG (opt.) | Python, FastAPI, embedchain (sidecar)  |

## Project Structure

```
realtime-chat/
├── pom.xml                                  # Maven build config
├── mvnw, mvnw.cmd                           # Maven wrapper scripts
├── .mvn/wrapper/maven-wrapper.properties
├── src/main/
│   ├── java/com/example/chat/
│   │   ├── ChatApplication.java             # Spring Boot entry point
│   │   ├── config/
│   │   │   ├── WebSocketConfig.java         # STOMP endpoint + broker config
│   │   │   └── RagProperties.java           # RAG sidecar settings
│   │   ├── controller/
│   │   │   ├── ChatController.java          # STOMP @MessageMapping handlers
│   │   │   ├── PresenceController.java      # GET /api/presence
│   │   │   └── AutocompleteController.java  # GET /api/autocomplete
│   │   ├── model/
│   │   │   ├── ChatMessage.java             # Chat payload (CHAT / JOIN / LEAVE)
│   │   │   └── PresenceSnapshot.java        # Online-user response
│   │   └── service/
│   │       ├── ChatService.java             # Message processing
│   │       ├── PresenceService.java         # Online-user registry
│   │       ├── AutocompleteService.java     # Typeahead dictionary
│   │       ├── BotService.java              # /ask command → RAG reply
│   │       └── RagClient.java               # HTTP client for the sidecar
│   └── resources/
│       ├── application.properties           # server.port, rag.* settings
│       └── static/
│           ├── index.html                   # Web chat client
│           └── vendor/                      # SockJS + stomp.js (vendored locally)
├── src/test/java/com/example/chat/          # JUnit 5 test suite
├── docs/UML.md                              # Architecture diagrams (Mermaid)
└── rag-service/                             # Optional embedchain RAG sidecar
```

## Getting Started

### Prerequisites

- [JDK 21](https://adoptium.net/) (or newer)
- Nothing else — Maven is bundled via the wrapper

### Run the app

```bash
./mvnw clean package          # Windows: mvnw.cmd clean package
./mvnw spring-boot:run        # or: java -jar target/chat-0.1.0.jar
```

Then open **http://localhost:8080** in two browser tabs, enter a name, and start chatting.

To change the port, edit `server.port` in `src/main/resources/application.properties` and restart.

## How It Works

1. The browser connects to `/ws` using SockJS and establishes a STOMP session.
2. The client subscribes to the broadcast topic `/topic/public`.
3. Messages sent to `/app/chat.sendMessage` are handled by `ChatController.sendMessage()`, processed by `ChatService`, and broadcast to all subscribers via `/topic/public` (`@SendTo`).
4. Join events go to `/app/chat.addUser`; every join registers the sender in `PresenceService` and the resulting JOIN frame updates the client's online-user list.
5. As the user types, the composer asks `/api/autocomplete?q=<prefix>` and shows suggestions; `AutocompleteService` learns words from every chat message.

```
Browser ──STOMP──▶ /app/chat.sendMessage   (client → server)
   ▲                                      ChatController → ChatService
   │                                            │
   └────────── /topic/public ◀─────────────────┘   (server → all clients)
```

### WebSocket Config (`WebSocketConfig.java`)

| Setting                    | Value                | Purpose                              |
|----------------------------|----------------------|--------------------------------------|
| Simple broker destinations | `/topic`             | Server-to-client broadcasts          |
| Application prefix         | `/app`               | Client-to-server `@MessageMapping`   |
| SockJS endpoint            | `/ws`                | WebSocket handshake endpoint         |

### HTTP endpoints

| Method | Path                          | Description                         |
|--------|-------------------------------|-------------------------------------|
| GET    | `/api/presence`               | Online users and count              |
| GET    | `/api/autocomplete?q=&limit=` | Typeahead suggestions               |

## RAG Bot (optional)

The `rag-service/` folder contains a small Python **embedchain** sidecar that
answers questions from a `docs/` folder. When enabled, messages prefixed with
`/ask` are answered by the bot and broadcast to the room.

```bash
cd rag-service
python -m venv .venv && . .venv/Scripts/activate   # Windows; use .venv/bin/activate on Unix
pip install -r requirements.txt
cp .env.example .env            # then set OPENAI_API_KEY
uvicorn app:app --host 0.0.0.0 --port 8000
```

Then enable it in `src/main/resources/application.properties`:

```properties
rag.enabled=true
rag.base-url=http://localhost:8000
rag.command=/ask
rag.bot-name=Assistant
```

See [`rag-service/README.md`](rag-service/README.md) for details.

## Extending

- **Private/group chats**: add destinations like `/user` with `convertAndSendToUser` and a destination prefix (`/user`).
- **Persistence**: add `spring-boot-starter-data-jpa` + a DB, and a listener on the broker to store messages.
- **External broker**: swap the simple broker for RabbitMQ (`spring-boot-starter-amqp`) or ActiveMQ for multi-instance scaling.
- **Auth**: protect the endpoint with Spring Security and resolve the sender from the STOMP `CONNECT` frame headers.

## Testing

Tests use JUnit 5 and are run by the Maven Surefire (unit) and Failsafe
(integration) plugins:

```bash
./mvnw test          # unit + slice tests (*Test)
./mvnw verify        # also runs the WebSocket integration test (*IT)
```

Run a single class with `./mvnw test -Dtest=ChatServiceTest`.

The SockJS info endpoint can be probed to confirm WebSocket support:

```bash
curl http://localhost:8080/ws/info   # → {"websocket":true, ...}
```

## Architecture

See [`docs/UML.md`](docs/UML.md) for component, class, sequence, state, and
deployment diagrams.

## Security

Please report vulnerabilities privately — see [`SECURITY.md`](SECURITY.md).
Dependency updates are automated with [Dependabot](.github/dependabot.yml), and
the project is scanned with [CodeQL](.github/workflows/codeql.yml) and the
[OpenSSF Scorecard](.github/workflows/scorecard.yml).

## Changelog

See [`CHANGELOG.md`](CHANGELOG.md).

## Contributing

Contributions are welcome! Please read [`CONTRIBUTING.md`](CONTRIBUTING.md) and
the [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md) first.

## License

Released under the [MIT License](LICENSE).
