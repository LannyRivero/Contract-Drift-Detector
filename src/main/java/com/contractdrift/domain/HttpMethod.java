package com.contractdrift.domain;

/**
 * Supported HTTP methods for API operations.
 *
 * <p>
 * Used by {@link EndpointKey} to uniquely identify an operation within a
 * contract alongside the path.
 */
public enum HttpMethod {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
    HEAD,
    OPTIONS,
    TRACE
}
