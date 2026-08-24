package com.contractdrift.domain;

import java.util.Map;
import java.util.Set;

public record ApiSchema(
        String name,
        String type,
        Map<String, ApiProperty> properties,
        Set<String> requiredProperties
) {
}
