# UML Diagrams

Architecture and interaction diagrams for Realtime Chat. All diagrams are
written in [Mermaid](https://mermaid.js.org/) and render directly on GitHub.

## 1. Component Diagram

```mermaid
flowchart LR
    subgraph Browser["Browser (web client)"]
        UI[index.html<br/>SockJS + @stomp/stompjs]
    end

    subgraph Server["Spring Boot Application"]
        WS[WebSocketConfig<br/>/ws endpoint + simple broker]
        CC[ChatController<br/>STOMP @MessageMapping]
        PC[PresenceController<br/>GET /api/presence]
        AC[AutocompleteController<br/>GET /api/autocomplete]
        CS[ChatService<br/>message processing]
        PS[PresenceService<br/>online registry]
        AS[AutocompleteService<br/>typeahead dictionary]
        BS[BotService<br/>/ask command]
        RC[RagClient<br/>HTTP client]
        CM[ChatMessage]
    end

    subgraph Sidecar["Python RAG sidecar"]
        FAST[FastAPI app.py]
        EC[embedchain App]
        DOCS[(docs/ folder)]
    end

    UI -- "STOMP over SockJS" --> WS
    WS --> CC
    CC --> CS
    CC --> BS
    CS --> PS
    CS --> AS
    BS --> RC
    RC -- "HTTP POST /query" --> FAST
    FAST --> EC
    EC --> DOCS
    PC --> PS
    AC --> AS
    CS --> CM
    CC -. "@SendTo /topic/public" .-> WS
    WS -. broadcast .-> UI
    UI -- "GET /api/presence" --> PC
    UI -- "GET /api/autocomplete" --> AC
```

## 2. Class Diagram

```mermaid
classDiagram
    class ChatApplication {
        +main(String[] args) void
    }

    class WebSocketConfig {
        +configureMessageBroker(MessageBrokerRegistry) void
        +registerStompEndpoints(StompEndpointRegistry) void
    }

    class ChatController {
        -ChatService chatService
        -BotService botService
        +sendMessage(ChatMessage) ChatMessage
        +addUser(ChatMessage) ChatMessage
        +leaveUser(ChatMessage) ChatMessage
    }

    class PresenceController {
        -PresenceService presenceService
        +presence() PresenceSnapshot
    }

    class AutocompleteController {
        -AutocompleteService autocompleteService
        +suggestions(String, Integer) List~String~
    }

    class ChatService {
        -PresenceService presenceService
        -AutocompleteService autocompleteService
        +handleChat(ChatMessage) ChatMessage
        +handleJoin(ChatMessage) ChatMessage
        +handleLeave(ChatMessage) ChatMessage
        +onlineUsers() Set~String~
    }

    class PresenceService {
        -Set~String~ users
        +register(String) void
        +remove(String) void
        +online() Set~String~
        +count() int
    }

    class AutocompleteService {
        -Map~String,Integer~ frequency
        -Map~String,String~ display
        +record(String) void
        +suggest(String, Integer) List~String~
        +size() int
    }

    class BotService {
        -RagClient ragClient
        -RagProperties properties
        -SimpMessagingTemplate messagingTemplate
        +maybeReply(ChatMessage) void
    }

    class RagClient {
        -RagProperties properties
        -RestClient restClient
        +ask(String) Optional~String~
    }

    class RagProperties {
        +boolean enabled
        +String baseUrl
        +String command
        +String botName
    }

    class ChatMessage {
        <<enumeration>> MessageType
        CHAT
        JOIN
        LEAVE
        -MessageType type
        -String content
        -String sender
        -long timestamp
        +getType() MessageType
        +getSender() String
        +getContent() String
        +getTimestamp() long
    }

    class PresenceSnapshot {
        <<record>>
        +Set~String~ users
        +int count
    }

    ChatController --> ChatService : delegates
    ChatController --> BotService : delegates
    PresenceController --> PresenceService : delegates
    AutocompleteController --> AutocompleteService : delegates
    ChatService --> PresenceService : tracks users
    ChatService --> AutocompleteService : records words
    BotService --> RagClient : queries
    RagClient --> RagProperties : reads
    BotService --> RagProperties : reads
    ChatController ..> ChatMessage : consumes
    ChatService ..> ChatMessage : returns
    PresenceController ..> PresenceSnapshot : returns
    WebSocketConfig ..> ChatController : routes to
```

## 3. Sequence Diagram — Join and Send a Message

```mermaid
sequenceDiagram
    autonumber
    actor Alice as Alice (browser)
    participant WS as WebSocketConfig / Broker
    participant CC as ChatController
    participant CS as ChatService
    participant PS as PresenceService
    participant AS as AutocompleteService

    Note over Alice,AS: Connection + Join
    Alice->>WS: SockJS connect to /ws
    WS-->>Alice: STOMP CONNECTED
    Alice->>WS: SEND /app/chat.addUser {type: JOIN}
    WS->>CC: addUser(message)
    CC->>CS: handleJoin(message)
    CS->>PS: register(sender)
    CS-->>CC: normalized message
    CC-->>WS: @SendTo /topic/public
    WS-->>Alice: JOIN broadcast

    Note over Alice,AS: Send a chat message
    Alice->>WS: SEND /app/chat.sendMessage {type: CHAT}
    WS->>CC: sendMessage(message)
    CC->>CS: handleChat(message)
    CS->>AS: record(content)
    CS-->>CC: normalized message
    CC-->>WS: @SendTo /topic/public
    WS-->>Alice: CHAT broadcast to all subscribers
```

## 4. Sequence Diagram — `/ask` Bot Answer

```mermaid
sequenceDiagram
    autonumber
    actor Alice as Alice (browser)
    participant WS as Broker
    participant CC as ChatController
    participant BS as BotService
    participant RC as RagClient
    participant RAG as RAG sidecar (FastAPI)

    Alice->>WS: SEND /app/chat.sendMessage {content: "/ask ..."}
    WS->>CC: sendMessage(message)
    CC-->>WS: @SendTo /topic/public (echoes user message)
    CC->>BS: maybeReply(message)
    BS->>BS: extract question after command
    BS->>RC: ask(question)
    RC->>RAG: HTTP POST /query {question}
    RAG-->>RC: {answer}
    RC-->>BS: Optional[answer]
    BS->>WS: convertAndSend /topic/public (sender = Assistant)
    WS-->>Alice: bot answer broadcast
```

## 5. State Diagram — Presence Lifecycle

```mermaid
stateDiagram-v2
    [*] --> Offline
    Offline --> Online : JOIN received (register)
    Online --> Online : CHAT received
    Online --> Offline : LEAVE received (remove)
    Offline --> [*]
```

## 6. Deployment Diagram

```mermaid
flowchart TB
    subgraph Client["Client machine"]
        B["Web browser<br/>http://localhost:8080"]
    end

    subgraph JVM["JVM — java -jar chat-0.1.0.jar"]
        T["Tomcat<br/>port 8080"]
        APP["Spring Application Context"]
        T --> APP
    end

    subgraph Py["Python sidecar (optional)"]
        U["uvicorn<br/>port 8000"]
    end

    B -- "HTTP + WebSocket (SockJS / STOMP)" --> T
    APP -- "HTTP POST /query" --> U
```

## Regenerating

These diagrams are source-of-truth Mermaid text. To view them locally, open
this file in any Mermaid-aware editor (VS Code with the Mermaid extension,
GitHub, or <https://mermaid.live>). Update the diagrams whenever the
architecture no longer matches the code.
