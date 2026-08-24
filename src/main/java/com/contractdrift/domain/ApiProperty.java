package com.contractdrift.domain;

/**
 * Simplified representation of a property inside an OpenAPI schema.
 *
 * <p>
 * Used by {@link ApiSchema} to model request body and response body fields.
 * Captures only name, type, and whether the property is required — enough
 * for the deterministic diff rules.
 *
 * @param name     property name
 * @param type     schema type, e.g. {@code "string"}, {@code "integer"}
 * @param required whether this property is mandatory in the schema
 */
public record ApiProperty(
                String name,
                String type,
                boolean required) {
}
