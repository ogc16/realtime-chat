# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- OpenSSF Scorecard, CodeQL, and Dependabot automation
- JaCoCo code-coverage reporting in CI
- Security policy, issue/PR templates, and CODEOWNERS

## [0.1.0] - 2026-10-12

### Added

- Realtime chat over WebSocket/STOMP with SockJS fallback
- Join/leave notifications and a live online-users list
- Message autocomplete / typeahead in the composer, learned from the conversation
- Optional embedchain RAG sidecar (`rag-service/`) with `/ask` bot replies over HTTP
- Modular service layer: `ChatService`, `PresenceService`, `AutocompleteService`, `BotService`, `RagClient`
- HTTP endpoints `GET /api/presence` and `GET /api/autocomplete`
- JUnit 5 test suite (unit, slice, and WebSocket integration tests)
- Mermaid UML diagrams in `docs/UML.md`
- MIT license, code of conduct, and contributing guide

[Unreleased]: https://github.com/ogc16/realtime-chat/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/ogc16/realtime-chat/releases/tag/v0.1.0
