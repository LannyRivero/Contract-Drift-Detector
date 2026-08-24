package com.contractdrift.domain;

import java.util.List;

/**
 * Strategy interface for a single backward-compatibility rule.
 *
 * <p>
 * Each implementation detects ONE specific type of breaking change
 * between two versions of the same endpoint. The {@link DiffEngine}
 * orchestrates all registered rules and collects their results.
 *
 * <p>
 * Adding a new rule means creating a new class that implements this
 * interface — no existing code needs to change (Open/Closed Principle).
 */
public interface CompatibilityRule {

    /**
     * Applies this rule to a single endpoint that exists in both contracts.
     *
     * @param oldEndpoint the previous version of the endpoint
     * @param newEndpoint the new version of the endpoint
     * @param location    human-readable endpoint identifier, e.g.
     *                    {@code GET /users/{id}}
     * @return list of changes detected by this rule (empty if none)
     */
    List<Change> apply(Endpoint oldEndpoint, Endpoint newEndpoint, String location);
}
