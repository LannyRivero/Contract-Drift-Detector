package com.contractdrift.domain;

/**
 * Unique identifier of an API operation inside a contract.
 *
 * <p>
 * In OpenAPI, the same path can expose multiple operations through different
 * HTTP methods. For that reason, the endpoint identity is composed of both the
 * normalized path and the HTTP method.
 *
 * @param path   OpenAPI path, for example {@code /users/{id}}
 * @param method HTTP method associated with the operation
 */
public record EndpointKey(String path, HttpMethod method) {

    public String displayName() {
        return method + " " + path;
    }
}
