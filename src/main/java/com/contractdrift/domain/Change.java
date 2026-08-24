package com.contractdrift.domain;

public record Change(
        ChangeType type,
        Severity severity,
        String location,
        String oldValue,
        String newValue,
        String message
) {
}
