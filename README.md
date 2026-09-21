# Contract Drift Detector

Detect breaking changes between two OpenAPI contracts before they reach production.

Contract Drift Detector is a deterministic Java CLI and GitHub Action for API compatibility checks. It compares an old OpenAPI specification against a new one, reports supported breaking changes, and exits with a non-zero status when the new contract is not backward compatible.

## Project goal

Build a small, practical Java tool that protects API consumers from accidental contract-breaking changes.

The project prioritizes:

- Small MVP scope.
- Deterministic compatibility rules.
- Clean architecture without unnecessary layers.
- Strong automated tests.
- Simple CI/CD integration.
- A comparison engine that stays independent from HTTP, frameworks, and AI.

AI may be added later to explain the impact of detected changes, but it must not decide whether a change is breaking.

## Why this exists

API changes can break existing consumers even when the backend still compiles and tests pass.

Common examples:

- Removing an endpoint used by clients.
- Removing a query/path parameter.
- Making an optional parameter required.
- Removing request or response fields.
- Changing field types, for example `integer` to `string`.
- Removing documented response status codes.

These changes are easy to miss in code review. This tool turns contract compatibility into an automated CI/CD check.

The goal is to avoid heuristic noise: compatibility is decided by explicit rules, not by guessing from implementation code.

## Quick start

Build the project:

```bash
mvn clean package
```

Run the included Petstore example:

```bash
java -jar target/contract-drift-detector-1.3.0.jar \
  examples/petstore/v1.0.yaml \
  examples/petstore/v2.0-breaking.yaml
```

Expected result:

```text
=================================
API CONTRACT COMPATIBILITY REPORT
=================================

Breaking changes: 7

[BREAKING] DELETE /pets/{petId}
Endpoint removed

[BREAKING] GET /pets
Parameter removed: species

[BREAKING] GET /pets/{petId}
Parameter became required: token

[BREAKING] GET /pets/{petId}
Type changed: age from integer to string

[BREAKING] POST /pets
Request property removed: owner

[BREAKING] POST /pets
Request property removed: age

[BREAKING] POST /pets
Type changed: age from integer to string
```

See [`examples/petstore/`](examples/petstore/) for the full sample contracts.

## How it is used in a real project

Keep a baseline OpenAPI contract for the API version currently consumed by clients:

```text
api/baseline.yaml
```

Generate or commit the new contract from the current branch:

```text
api/current.yaml
```

Compare both contracts:

```bash
java -jar contract-drift-detector.jar api/baseline.yaml api/current.yaml
```

If the new contract contains supported breaking changes, the process exits with code `1`. That makes it useful as a pull request or release gate.

Typical workflow:

```text
Pull request changes OpenAPI contract
        ↓
Contract Drift Detector compares baseline vs current
        ↓
Supported breaking change found
        ↓
CI check fails before merge/deploy
```

## What it detects

| Change | Breaking? | Example |
|---|---:|---|
| Endpoint removed | Yes | `DELETE /users/{id}` no longer exists |
| Parameter removed | Yes | `?filter=active` was removed |
| New required parameter | Yes | `?token` is added as required |
| Parameter became required | Yes | `?token` optional -> required |
| Request property removed | Yes | `name` removed from request body |
| Request property became required | Yes | `email` optional -> required |
| Request property type changed | Yes | `age: integer` -> `age: string` |
| Response status removed | Yes | `404` response removed |
| Response property removed | Yes | `name` removed from response body |
| Response property type changed | Yes | `id: string` -> `id: integer` |

The detector is intentionally rule-based. It does not guess compatibility with AI or heuristics; each reported issue comes from an explicit compatibility rule.

## MVP scope

The MVP focuses on the compatibility checks that usually break existing REST API consumers:

- Removed endpoints or HTTP operations.
- Removed parameters.
- Parameters changed from optional to required.
- Newly added required parameters.
- Removed request body properties.
- Request body properties changed from optional to required.
- Request or response property type changes.
- Removed response status codes.
- Removed response body properties.

## Out of scope for the MVP

The project intentionally avoids product/platform features that would distract from the contract engine:

- Frontend or dashboard.
- User accounts, authentication, or billing.
- Database persistence.
- Kafka or microservice orchestration.
- SaaS packaging.
- Mandatory AI integration.
- Full OpenAPI edge-case coverage.
- Complex web UI.

New ideas should go to the roadmap first, not straight into the MVP.

## CLI usage

```bash
java -jar contract-drift-detector.jar <old-contract.yaml> <new-contract.yaml> [--json] [--config contract-drift.yaml]
```

Exit codes:

| Code | Meaning |
|---:|---|
| `0` | No breaking changes detected |
| `1` | Breaking changes detected, invalid arguments, or config loading error |

### JSON output

Use `--json` when another tool or CI job needs to consume the result:

```bash
java -jar contract-drift-detector.jar api/baseline.yaml api/current.yaml --json > contract-report.json
```

Example:

```json
{
  "summary": {
    "breaking": 2,
    "safe": 0,
    "total": 2
  },
  "hasBreakingChanges": true,
  "changes": [
    {
      "type": "ENDPOINT_REMOVED",
      "severity": "BREAKING",
      "location": "GET /users/{id}",
      "oldValue": "present",
      "newValue": "missing",
      "message": "Endpoint removed"
    }
  ]
}
```

## GitHub Actions usage

Use this action when your repository already has two OpenAPI files to compare:

```yaml
name: API compatibility

on:
  pull_request:
    paths:
      - "api/**"

jobs:
  contract-drift:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Check OpenAPI compatibility
        uses: LannyRivero/Contract-Drift-Detector@v1.1.0
        with:
          old-contract: api/baseline.yaml
          new-contract: api/current.yaml
          json-output: "true"
```

If a supported breaking change is detected, the action fails and blocks the workflow.

### Action inputs

| Input | Description | Required | Default |
|---|---|---|---|
| `old-contract` | Path to the old OpenAPI specification | Yes | - |
| `new-contract` | Path to the new OpenAPI specification | Yes | - |
| `json-output` | Output results as JSON | No | `false` |

## Configuration

Use a config file to ignore selected rule types:

```yaml
# contract-drift.yaml
ignoredRules:
  - PARAMETER_REMOVED
  - RESPONSE_PROPERTY_REMOVED
failOnWarning: false
```

Run with:

```bash
java -jar contract-drift-detector.jar old.yaml new.yaml --config contract-drift.yaml
```

Config options:

| Option | Description | Default |
|---|---|---|
| `ignoredRules` | Change types to ignore when rendering and evaluating the result | `[]` |
| `failOnWarning` | Reserved for warning-level policies | `false` |

Available rule/change types:

- `ENDPOINT_REMOVED`
- `PARAMETER_REMOVED`
- `PARAMETER_BECAME_REQUIRED`
- `REQUEST_PROPERTY_REMOVED`
- `REQUEST_PROPERTY_BECAME_REQUIRED`
- `REQUEST_PROPERTY_TYPE_CHANGED`
- `RESPONSE_REMOVED`
- `RESPONSE_PROPERTY_REMOVED`
- `RESPONSE_PROPERTY_TYPE_CHANGED`

## Architecture

```text
OpenAPI v1 ─┐
            ├── OpenAPI parser adapter
OpenAPI v2 ─┘
                    ↓
              Domain contract model
                    ↓
                Diff engine
                    ↓
            Compatibility rules
                    ↓
             Console / JSON report
```

Design choices:

- **Deterministic rules**: compatibility decisions are explicit and testable.
- **Parser-agnostic domain**: the diff engine works with domain records, not Swagger parser objects.
- **Small rule classes**: each compatibility check implements the same rule interface.
- **CLI-first**: the tool can run locally, in CI, or inside the Docker-based GitHub Action.
- **No Spring Web/API layer**: the comparison engine is standalone and can be used outside Spring Boot projects.

### Why there is no Spring Boot API yet

The original project idea allowed an optional Spring Boot API such as `POST /api/v1/compare`, but the comparison engine should not depend on HTTP.

For the MVP, the CLI gives the fastest useful path: local usage, CI execution, and GitHub Action integration. A Spring Boot API can be added later as a thin adapter around the same application use case if it adds real value.

### Future AI layer

If AI is added, it should sit after the deterministic report:

```text
Deterministic report
        ↓
Spring AI or another LLM integration
        ↓
Human-readable impact explanation
```

The rule engine decides what is breaking. AI only explains why the reported change may affect consumers.

## Project structure

```text
src/main/java/com/contractdrift/
├── ContractDriftDetectorApplication.java   # CLI entry point
├── application/
│   └── CompareContractsUseCase.java        # Compare orchestration
├── domain/
│   ├── Contract.java                       # API contract
│   ├── Endpoint.java                       # API operation
│   ├── EndpointKey.java                    # Endpoint identity
│   ├── Change.java                         # Detected change record
│   ├── ChangeType.java                     # Change categories
│   ├── Severity.java                       # BREAKING / NON_BREAKING
│   ├── CompatibilityRule.java              # Rule strategy interface
│   ├── DiffEngine.java                     # Rule orchestrator
│   └── rules/                              # Compatibility rules
│       ├── AddedRequiredParameterRule.java
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
    ├── config/                             # YAML config loading
    ├── openapi/
    │   ├── OpenApiParserAdapter.java       # YAML/OpenAPI -> domain contract
    │   └── OpenApiMapper.java              # Swagger model -> domain model
    └── report/
        ├── ConsoleReportRenderer.java      # Human-readable output
        └── JsonReportRenderer.java         # Machine-readable output
```

## Current limitations

This project currently focuses on deterministic OpenAPI compatibility rules. It does not detect yet:

- Authentication or authorization policy changes.
- Rate limit changes.
- Header compatibility changes.
- Schema composition details such as `allOf`, `oneOf`, and `anyOf`.
- Webhooks or event contracts.

## Development

Run tests:

```bash
mvn verify
```

Build the JAR:

```bash
mvn clean package
```

Tech stack:

- Java 21
- Maven
- Swagger Parser
- JUnit 5
- AssertJ

Testing expectations:

- Each compatibility rule should have focused tests.
- Rules should cover positive, negative, and basic edge cases where practical.
- Fixtures should stay small and readable.
- `mvn verify` should stay green before release or merge.

## Current MVP status

Implemented:

- OpenAPI parsing through Swagger Parser.
- Domain model for contracts, endpoints, schemas, parameters, and changes.
- Deterministic diff engine.
- Console report output.
- JSON report output for CI/CD.
- Config file support for ignored rule types.
- Docker-based GitHub Action metadata.
- GitHub Actions CI running `mvn verify` on push and pull requests to `main`.
- Automated test suite for the main rules and infrastructure pieces.

Still worth improving:

- Markdown or PR-comment output.
- More OpenAPI edge-case support.
- Release/version alignment between README examples, tags, and `pom.xml`.

## Roadmap

### v1.1

- JSON output for CI/CD integration.
- Detection for newly added required parameters.
- Config file support for ignored rule types.

### v1.2

- GitHub Action distribution improvements.
- Better release documentation and examples.

### v1.3

- Automatic PR comments with the compatibility report.

### v2.0

- Expanded policy file support.
- Header and authentication-related breaking changes.
- Better schema composition support for `allOf`, `oneOf`, and `anyOf`.

## License

MIT
