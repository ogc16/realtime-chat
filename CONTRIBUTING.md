# Contributing to Realtime Chat

Thanks for your interest in improving Realtime Chat! This document explains how
to set up the project, the conventions we follow, and how to get your changes
merged.

By participating you agree to follow our [Code of Conduct](CODE_OF_CONDUCT.md).

## Ways to Contribute

- Report bugs and request features via [GitHub Issues](https://github.com/ogc16/realtime-chat/issues).
- Improve documentation (README, this guide, UML diagrams in [`docs/UML.md`](docs/UML.md)).
- Submit code changes via a pull request.

## Development Setup

### Prerequisites

- [JDK 21](https://adoptium.net/) or newer
- Git
- No global Maven install required — the Maven Wrapper (`./mvnw`) is bundled

### Build and run

```bash
# Compile and package
./mvnw clean package          # Windows: mvnw.cmd clean package

# Run the app
./mvnw spring-boot:run        # then open http://localhost:8080
```

### Run the tests

The test suite uses JUnit 5 (Jupiter) and is executed by the Maven Surefire
plugin. See [Test Runner](#test-runner) below for details.

```bash
./mvnw test                   # unit + slice tests
./mvnw verify                 # includes *IT integration tests
```

## Project Layout

```
src/main/java/com/example/chat/
├── ChatApplication.java        # Spring Boot entry point
├── config/WebSocketConfig.java # STOMP endpoint + broker config
├── controller/                 # Inbound adapters (STOMP + HTTP)
├── model/ChatMessage.java      # Chat payload (CHAT / JOIN / LEAVE)
└── service/                    # Business logic (chat + presence)
```

Keep business logic in `service/`, keep controllers thin, and add a test under
`src/test/java/...` for every new behavior.

## Coding Conventions

- **Java 21**, 4-space indentation, braces on the same line.
- Follow the existing package structure (`config`, `controller`, `model`, `service`).
- Prefer constructor injection over field injection.
- Keep public APIs documented with Javadoc where the intent is not obvious.
- Do **not** add comments that merely restate the code.

## Branching and Commits

- Branch from `master` using `feature/<short-name>` or `fix/<short-name>`.
- Keep commits focused and use the imperative mood, e.g.
  `Add private messaging endpoint`.
- Reference the related issue in the commit body where applicable
  (`Refs #12`).

## Pull Request Process

1. Fork the repo and create your branch.
2. Make your change and add/update tests.
3. Run `./mvnw verify` and make sure everything passes.
4. Update the README or [`docs/UML.md`](docs/UML.md) if the change affects the
   public behavior or architecture.
5. Open the PR with a clear description of the change and how it was tested.

A maintainer will review the PR. Please keep discussions respectful and focused
on the change.

## Test Runner

Tests are run with the Maven Surefire plugin as part of `./mvnw test`. Two test
styles are used:

| Type        | Location        | Naming     | Runs in      |
|-------------|-----------------|------------|--------------|
| Unit/slice  | `src/test/java` | `*Test`    | `mvn test`   |
| Integration | `src/test/java` | `*IT`      | `mvn verify` |

Run a single test class with:

```bash
./mvnw test -Dtest=ChatServiceTest
```

The integration test (`ChatWebSocketIT`) starts a real WebSocket/STOMP session
and verifies the publish/subscribe round trip.

## Reporting Bugs

Please include:

- A clear description of the problem and what you expected to happen
- Steps to reproduce
- Your JDK and OS versions
- Any relevant logs or stack traces

## License

By contributing, you agree that your contributions are licensed under the
project's [MIT License](LICENSE).
