# Contract Drift Detector

Deterministic OpenAPI compatibility checker for detecting breaking API contract changes.

## Goal

Contract Drift Detector compares two OpenAPI contracts and reports incompatible API changes before they reach production.

The project prioritizes:

- small MVP scope
- deterministic rules
- clean architecture
- strong testing
- CI/CD readiness
- optional AI explanations only after the deterministic report exists

## MVP Scope

The first MVP will detect:

- removed endpoints
- removed HTTP methods
- removed parameters
- parameters becoming required
- incompatible type changes
- new required request properties
- removed response codes
- removed response properties

## Out of Scope for MVP

The MVP intentionally does not include:

- frontend
- authentication
- database
- Kafka
- dashboard
- SaaS features
- billing
- mandatory AI

New ideas go to the roadmap, not directly into the MVP.

## Tech Stack

- Java 21
- Spring Boot 3
- Maven
- JUnit 5
- AssertJ
- Swagger Parser for OpenAPI parsing
- GitHub Actions

## Architecture Principle

The comparison engine must not depend on HTTP, Spring MVC, databases, or AI.

Initial pipeline:

```text
OpenAPI v1 ─┐
            ├── Parser Adapter
OpenAPI v2 ─┘
                ↓
          Contract Model
                ↓
           Diff Engine
                ↓
              Report
```

## Current Status

Initial repository bootstrap.

First implementation target:

```text
Detect removed endpoints with domain-level tests.
```

## Run Tests

```bash
mvn verify
```
