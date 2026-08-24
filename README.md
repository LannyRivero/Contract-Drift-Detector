# 🧪 Contract Drift Detector

Deterministic OpenAPI compatibility checker for detecting breaking API contract changes.

## Problem

When an API evolves, small contract changes can break existing consumers:

- Removing an endpoint
- Removing a parameter
- Converting an optional parameter to required
- Changing a field type
- Adding a new required request property
- Removing a response code

These changes often slip through manual code reviews. Contract Drift Detector automates this check.

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
POST /users [200]
Type changed: email from string to integer

[BREAKING]
POST /users
New required field: country
```

## What It Detects

| Change Type | Severity |
|---|---|
| Endpoint removed | BREAKING |
| HTTP method removed | BREAKING |
| Parameter removed | BREAKING |
| Parameter became required | BREAKING |
| Required property added | BREAKING |
| Property type changed | BREAKING |
| Response code removed | BREAKING |
| Response property removed | BREAKING |
| Response property type changed | BREAKING |

## What It Does NOT Detect (Yet)

- Authentication changes
- Rate limiting changes
- Header changes
- All OpenAPI edge cases
- Webhook changes
- Schema composition (allOf, oneOf, anyOf)

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
- **Parser-agnostic** — the diff engine works on domain objects, not Swagger POJOs
- **No Spring dependency** — the engine runs without Spring Boot; the CLI is standalone

## Project Structure

```text
src/main/java/com/contractdrift/
├── ContractDriftDetectorApplication.java   # CLI entry point
├── application/
│   └── CompareContractsUseCase.java        # Orchestration
├── domain/
│   ├── Change.java                         # Change record
│   ├── ChangeType.java                     # Change categories
│   ├── Contract.java                       # API contract
│   ├── ContractDiffer.java                 # Diff engine
│   ├── Endpoint.java                       # API operation
│   ├── EndpointKey.java                    # Endpoint identity
│   └── Severity.java                       # BREAKING / NON_BREAKING
└── infrastructure/
    ├── openapi/
    │   └── OpenApiParserAdapter.java       # YAML → Contract
    └── report/
        └── ConsoleReportRenderer.java      # Changes → text
```

## Tech Stack

- Java 21
- Maven
- Swagger Parser 2.1.46
- JUnit 5
- AssertJ
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
- JSON output
- Markdown output
- Compatibility score

### v1.2
- GitHub Action

### v1.3
- Automatic PR comments

### v1.4
- Optional AI explanations (Spring AI)

### v2.0
- Configurable rules
- Ignore rules
- Policy file
- Multiple compatibility profiles

## License

MIT
