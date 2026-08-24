package com.contractdrift.domain;

public record ApiProperty(
        String name,
        String type,
        boolean required
) {
}
