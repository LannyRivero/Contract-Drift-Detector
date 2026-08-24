package com.contractdrift.domain;

/**
 * Simplified representation of an OpenAPI parameter.
 *
 * <p>
 * Captures the fields relevant for backward compatibility checks: name,
 * location (path, query, header, cookie), whether it is required, and
 * its schema type.
 *
 * @param name     parameter name
 * @param location parameter location: {@code "path"}, {@code "query"},
 *                 {@code "header"}, {@code "cookie"}
 * @param required whether the parameter is mandatory
 * @param type     schema type, e.g. {@code "string"}, {@code "integer"}
 */
public record ApiParameter(
                String name,
                String location,
                boolean required,
                String type) {
}
