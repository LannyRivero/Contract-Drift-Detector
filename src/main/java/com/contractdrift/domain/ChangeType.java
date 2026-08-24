package com.contractdrift.domain;

/**
 * Categories of changes that can be detected between two API contracts.
 *
 * <p>
 * Each constant represents a specific type of backward-incompatible or
 * compatible change. The {@link ContractDiffer} maps detected differences
 * to these types so that renderers and downstream tools can handle them
 * uniformly.
 */
public enum ChangeType {

    /** An entire endpoint (path + method) was removed. */
    ENDPOINT_REMOVED,

    /** A new endpoint was added (non-breaking). */
    ENDPOINT_ADDED,

    /** A parameter was removed from an endpoint. */
    PARAMETER_REMOVED,

    /** An optional parameter became required. */
    PARAMETER_BECAME_REQUIRED,

    /** The type of a parameter changed incompatibly. */
    PARAMETER_TYPE_CHANGED,

    /** A property was removed from the request body schema. */
    REQUEST_PROPERTY_REMOVED,

    /** An optional request body property became required. */
    REQUEST_PROPERTY_BECAME_REQUIRED,

    /** The type of a request body property changed incompatibly. */
    REQUEST_PROPERTY_TYPE_CHANGED,

    /** A response status code was removed. */
    RESPONSE_REMOVED,

    /** A property was removed from a response schema. */
    RESPONSE_PROPERTY_REMOVED,

    /** The type of a response property changed incompatibly. */
    RESPONSE_PROPERTY_TYPE_CHANGED
}
