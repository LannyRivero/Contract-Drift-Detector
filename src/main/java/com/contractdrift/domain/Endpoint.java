package com.contractdrift.domain;

import java.util.List;
import java.util.Map;

public record Endpoint(
        EndpointKey key,
        List<ApiParameter> parameters,
        ApiSchema requestBody,
        Map<String, ApiSchema> responses
) {
}
