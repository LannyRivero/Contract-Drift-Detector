package com.contractdrift.domain;

/**
 * Represents a single difference detected between two API contracts.
 *
 * <p>
 * Each change captures what changed ({@link ChangeType}), how severe it is
 * ({@link Severity}), where it occurred, and a human-readable message. The
 * diff engine produces a list of these objects; renderers consume them to
 * produce reports in different formats (console, JSON, Markdown).
 *
 * @param type     category of the change (endpoint removed, type changed, etc.)
 * @param severity impact level: {@code BREAKING} or {@code NON_BREAKING}
 * @param location human-readable location, e.g. {@code GET /users/{id}}
 * @param oldValue previous value (for type changes) or {@code "present"}
 * @param newValue new value (for type changes) or {@code "missing"}
 * @param message  description of what happened
 */
public record Change(
                ChangeType type,
                Severity severity,
                String location,
                String oldValue,
                String newValue,
                String message) {
}
