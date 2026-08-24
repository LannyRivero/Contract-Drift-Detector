package com.contractdrift.domain;

public record ApiParameter(
        String name,
        String location,
        boolean required,
        String type
) {
}
