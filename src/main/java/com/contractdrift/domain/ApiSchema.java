package com.contractdrift.domain;

import java.util.Map;
import java.util.Set;

/**
 * Simplified representation of an OpenAPI schema object.
 *
 * <p>
 * Captures only the information needed for backward compatibility checks:
 * the schema type, its properties, and which properties are required. Nested
 * schemas and advanced OpenAPI features (allOf, oneOf, anyOf) are intentionally
 * excluded from the MVP scope.
 *
 * @param name               optional schema name (from
 *                           {@code components/schemas})
 * @param type               schema type, e.g. {@code "object"},
 *                           {@code "string"}
 * @param properties         map of property name to {@link ApiProperty}
 * @param requiredProperties set of property names that are required
 */
public record ApiSchema(
                String name,
                String type,
                Map<String, ApiProperty> properties,
                Set<String> requiredProperties) {
}
