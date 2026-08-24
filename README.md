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

## What It Detects (v1.1)

| Change Type | Severity | Example |
|---|---|---|
| Endpoint removed | BREAKING | `DELETE /users/{id}` deleted |
| Parameter removed | BREAKING | `?filter=active` removed |
| New required parameter | BREAKING | `?token` added as required |
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
│   └── rules/                              # 10 compatibility rules
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
    ├── openapi/
    │   ├── OpenApiParserAdapter.java       # YAML → Contract
    │   └── OpenApiMapper.java              # Swagger → Domain
    └── report/
        ├── ConsoleReportRenderer.java      # Changes → text
        └── JsonReportRenderer.java         # Changes → JSON
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
java -jar target/contract-drift-detector-1.0.0.jar old-api.yaml new-api.yaml
```

Exit code:
- `0` — no breaking changes
- `1` — breaking changes detected

### JSON Output

For CI/CD integration, use `--json`:

```bash
java -jar target/contract-drift-detector-1.0.0.jar old-api.yaml new-api.yaml --json
```

Output:

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

### CI/CD Example

```yaml
# .github/workflows/api-compat.yml
- name: Check API compatibility
  run: |
    java -jar contract-drift-detector.jar \
      baseline.yaml \
      new-version.yaml \
      --json > report.json
    
    if [ $? -ne 0 ]; then
      echo "Breaking changes detected!"
      cat report.json
      exit 1
    fi
```

## GitHub Action

Use in your workflow:

```yaml
- name: Check API compatibility
  uses: LannyRivero/Contract-Drift-Detector@v1.1.0
  with:
    old-contract: 'api/baseline.yaml'
    new-contract: 'api/current.yaml'
    json-output: 'true'
```

### Inputs

| Input | Description | Required | Default |
|---|---|---|---|
| `old-contract` | Path to old OpenAPI spec | Yes | - |
| `new-contract` | Path to new OpenAPI spec | Yes | - |
| `json-output` | Output as JSON | No | `false` |

### Example with version check

```yaml
name: API Compatibility
on:
  pull_request:
    paths:
      - 'api/**'

jobs:
  check-compatibility:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Check API compatibility
        uses: LannyRivero/Contract-Drift-Detector@v1.1.0
        with:
          old-contract: 'api/main.yaml'
          new-contract: 'api/pr.yaml'
          json-output: 'true'
```

## Real-World Example

Run against the Petstore API demo:

```bash
java -jar target/contract-drift-detector-1.0.0.jar \
  examples/petstore/v1.0.yaml \
  examples/petstore/v2.0-breaking.yaml
```

Output:
```
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

See [`examples/petstore/`](examples/petstore/) for full contracts.

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
