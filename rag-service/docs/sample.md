# Realtime Chat — Knowledge Base

This document is embedded by the RAG sidecar and answers questions asked with
the `/ask` command.

## What is this project?

Realtime Chat is a small chat application built with Spring Boot and Spring
WebSocket (STOMP). Browsers connect with SockJS, subscribe to the `/topic/public`
topic, and exchange messages over the `/app/chat.sendMessage` and
`/app/chat.addUser` destinations.

## How do I run it?

Build with `./mvnw clean package` and run with `./mvnw spring-boot:run`. Then
open http://localhost:8080 in two browser tabs and join with a display name.
The default port is 8080 and can be changed via `server.port` in
`application.properties`.

## How does autocomplete work?

As you type in the message box, the client calls
`GET /api/autocomplete?q=<prefix>`. The `AutocompleteService` learns words from
every chat message and returns the most frequent matching words. Use the arrow
keys to highlight a suggestion and Tab to accept it.

## What is the `/ask` command?

When the RAG sidecar is enabled, a message that starts with `/ask` is forwarded
to the embedchain service, and the answer is broadcast to the room as a message
from the bot (default name "Assistant").

## Where are the tests?

Unit and slice tests run with `./mvnw test`. The WebSocket integration test runs
with `./mvnw verify` (Maven Failsafe) against `ChatWebSocketIT`.
