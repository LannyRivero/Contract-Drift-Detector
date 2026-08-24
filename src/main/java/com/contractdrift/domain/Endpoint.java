package com.contractdrift.domain;

import java.util.List;
import java.util.Map;

/**
 * Domain representation of a single API operation.
 *
 * <p>
 * An endpoint contains the parts relevant for backward compatibility checks:
 * parameters, request body schema and response schemas. It does not try to
 * model
 * every OpenAPI feature; only the information needed by the deterministic diff
 * rules belongs here.
 *
 * @param key         unique endpoint identifier
 * @param parameters  operation parameters such as query, path or header
 *                    parameters
 * @param requestBody request body schema, when the operation accepts one
 * @param responses   response schemas indexed by HTTP status code
 */
public record Endpoint(
                EndpointKey key,
                List<ApiParameter> parameters,
                ApiSchema requestBody,
                Map<String, ApiSchema> responses) {
}
