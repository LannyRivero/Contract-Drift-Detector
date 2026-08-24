package com.contractdrift.domain;

import java.util.Map;

/**
 * Domain representation of an API contract after parsing an OpenAPI document.
 *
 * <p>
 * The contract is modeled as a collection of endpoints indexed by path and
 * HTTP method. This keeps the diff engine independent from parser-specific
 * classes such as Swagger's {@code OpenAPI}, {@code PathItem} or
 * {@code Operation}.
 *
 * @param endpoints endpoints exposed by the contract, indexed by their unique
 *                  key
 */

public record Contract(Map<EndpointKey, Endpoint> endpoints) {
}
