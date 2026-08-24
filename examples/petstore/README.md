# Petstore API Examples

Real-world demo contracts for Contract Drift Detector.

## Files

| File | Description |
|---|---|
| `v1.0.yaml` | Original API with 3 endpoints |
| `v2.0-breaking.yaml` | Breaking version with 7 changes |

## Breaking Changes Detected

```
$ contract-drift detect v1.0.yaml v2.0-breaking.yaml

[ERROR] DELETE /pets/{petId} — Endpoint removed
[ERROR] GET /pets — Parameter removed: species
[ERROR] GET /pets/{petId} — Parameter became required: token
[ERROR] GET /pets/{petId} — Type changed: age from integer to string
[ERROR] POST /pets — Request property removed: owner
[ERROR] POST /pets — Request property removed: age
[ERROR] POST /pets — Type changed: age from integer to string
```

## Use Case

In a real team, you would run this in CI:

```yaml
# .github/workflows/api-compat.yml
- name: Check API compatibility
  run: |
    java -jar contract-drift-detector.jar \
      baseline.yaml \
      new-version.yaml
```

If any breaking changes are detected, the pipeline fails (exit code 1).
