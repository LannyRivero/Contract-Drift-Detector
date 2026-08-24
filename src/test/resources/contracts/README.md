# Test Contracts

This directory contains OpenAPI specification fixtures used by the test suite.

## Design Principles

1. **One fixture = one scenario** — each file tests a single breaking change
2. **Minimal but realistic** — enough structure to be realistic, not bloated
3. **Baseline as reference** — all breaking fixtures are diffs from `baseline.yaml`

## Fixtures

| Fixture | Breaking Change | Lines |
|---|---|---|
| `baseline.yaml` | Reference contract (v2.0.0) | ~90 |
| `endpoint-removed.yaml` | DELETE /users/{id} removed | ~70 |
| `parameter-removed.yaml` | `sort` query param removed | ~40 |
| `parameter-became-required.yaml` | `status` changed from optional to required | ~40 |
| `type-changed.yaml` | `email` changed from string to integer | ~30 |
| `response-removed.yaml` | 404 response removed | ~30 |
| `compatible-change.yaml` | `limit` param added (non-breaking) | ~40 |

## Usage

### Integration Tests (OpenApiParserAdapterTest)

```java
Path baseline = Path.of("src/test/resources/contracts/baseline.yaml");
Contract contract = parser.parse(baseline);
```

### Unit Tests (DiffEngineTest)

Unit tests use in-memory contracts, not YAML files:

```java
Contract oldContract = contractWith(key, List.of(oldParam));
Contract newContract = contractWith(key, List.of(newParam));
List<Change> changes = engine.diff(oldContract, newContract);
```

## Adding New Fixtures

1. Create a new YAML file with ONE breaking change
2. Add a `@Test` in the appropriate test class
3. Update this README
