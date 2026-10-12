# Security Policy

## Supported Versions

Only the latest released version is supported with security updates.

| Version | Supported          |
|---------|--------------------|
| 0.1.x   | :white_check_mark: |
| < 0.1   | :x:                |

## Reporting a Vulnerability

Please **do not** open a public issue for security problems.

Instead, report it privately using GitHub's
[private vulnerability reporting](https://github.com/ogc16/realtime-chat/security/advisories/new)
(Security → Advisories → "Report a vulnerability").

Please include:

- A description of the vulnerability and its impact
- Steps to reproduce or a proof of concept
- Affected version(s) and environment
- Any suggested remediation

You can expect an acknowledgement within a few days. Once the issue is
confirmed and fixed, a GitHub Security Advisory will be published and credit
will be given to the reporter unless you prefer to remain anonymous.

## Scope

This project is a demo/reference chat application. The following are in scope:

- The Spring Boot application and its HTTP/STOMP endpoints (`src/`)
- The RAG sidecar in `rag-service/`

Out of scope: vulnerabilities in third-party dependencies (report those
upstream), and issues that require a already-compromised host.
