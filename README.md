# Contract Drift Detector

Deterministic OpenAPI compatibility checker for detecting breaking API contract changes.

## Problem

When an API evolves, small contract changes can break existing consumers:

- Removing an endpoint
- Removing a parameter
- Converting an optional parameter to required
- Removing a request body property
- Changing a field type
- Removing a response code

These changes often slip through manual code reviews. Contract Drift Detector automates this check with **zero false positives** — every detection is a real breaking change.

## Example

Given two OpenAPI contracts:

```bash
java -jar contract-drift-detector.jar old-api.yaml new-api.yaml
```

Output:

```
=================================
API CONTRACT COMPATIBILITY REPORT
=================================

Breaking changes: 3
Safe changes: 0

[BREAKING]
GET /users/{id}
Endpoint removed

[BREAKING]
POST /orders
New required field: country

[BREAKING]
GET /products/{id}
Type changed: price from string to number
```

## What It Detects (v1.0)

| Change Type | Severity | Example |
|---|---|---|
| Endpoint removed | BREAKING | `DELETE /users/{id}` deleted |
| Parameter removed | BREAKING | `?filter=active` removed |
| Parameter became required | BREAKING | `?token` optional → required |
| Request property removed | BREAKING | `name` field removed from body |
| Request property became required | BREAKING | `email` optional → required |
| Request property type changed | BREAKING | `age: string` → `age: integer` |
| Response code removed | BREAKING | `404` response deleted |
| Response property removed | BREAKING | `name` field removed from response |
| Response property type changed | BREAKING | `id: string` → `id: integer` |

## What It Does NOT Detect (Yet)

- Authentication changes
- Rate limiting changes
- Header changes
- Schema composition (allOf, oneOf, anyOf)
- Webhook changes

These are planned for future versions.

## Architecture

```text
OpenAPI v1 ─┐
            ├── Parser Adapter (Swagger Parser)
OpenAPI v2 ─┘
                ↓
          Contract Model (Domain)
                ↓
           Diff Engine
                ↓
         Compatibility Rules
                ↓
              Report
```

### Key Design Decisions

- **Deterministic rules only** — AI can explain impact later, but never decides if something is breaking
- **Domain-driven** — `Contract`, `Endpoint`, `Change` are plain Java records
- **Strategy pattern** — each rule is a separate class implementing `CompatibilityRule`
- **Parser-agnostic** — the diff engine works on domain objects, not Swagger POJOs
- **No Spring dependency** — the engine runs without Spring Boot; the CLI is standalone

## Project Structure

```text
src/main/java/com/contractdrift/
├── ContractDriftDetectorApplication.java   # CLI entry point
├── application/
│   └── CompareContractsUseCase.java        # Orchestration
├── domain/
│   ├── Contract.java                       # API contract
│   ├── Endpoint.java                       # API operation
│   ├── EndpointKey.java                    # Endpoint identity
│   ├── Change.java                         # Change record
│   ├── ChangeType.java                     # Change categories
│   ├── Severity.java                       # BREAKING / NON_BREAKING
│   ├── CompatibilityRule.java              # Strategy interface
│   ├── DiffEngine.java                     # Rule orchestrator
│   └── rules/                              # 9 compatibility rules
│       ├── RemovedEndpointRule.java
│       ├── RemovedParameterRule.java
│       ├── ParameterBecameRequiredRule.java
│       ├── RemovedRequestPropertyRule.java
│       ├── RequestPropertyBecameRequiredRule.java
│       ├── RequestPropertyTypeChangedRule.java
│       ├── RemovedResponseRule.java
│       ├── ResponsePropertyRemovedRule.java
│       └── ResponseTypeChangedRule.java
└── infrastructure/
    ├── openapi/
    │   ├── OpenApiParserAdapter.java       # YAML → Contract
    │   └── OpenApiMapper.java              # Swagger → Domain
    └── report/
        └── ConsoleReportRenderer.java      # Changes → text
```

## Tech Stack

- Java 21
- Maven
- Swagger Parser 2.1.46
- JUnit 5 + AssertJ
- GitHub Actions

## Run Tests

```bash
mvn verify
```

## Build

```bash
mvn clean package
```

## Usage

```bash
java -jar target/contract-drift-detector-0.0.1-SNAPSHOT.jar old-api.yaml new-api.yaml
```

Exit code:
- `0` — no breaking changes
- `1` — breaking changes detected

## CI/CD

GitHub Actions runs `mvn verify` on every push and PR to `main`.

## Roadmap

### v1.1
- JSON output for CI/CD integration
- AddedRequiredParameter rule (new endpoint with required params)

### v1.2
- GitHub Action

### v1.3
- Automatic PR comments

### v2.0
- Configurable rules
- Policy file

## License

MIT
