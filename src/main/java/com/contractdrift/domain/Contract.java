package com.contractdrift.domain;

import java.util.Map;

public record Contract(Map<EndpointKey, Endpoint> endpoints) {
}
