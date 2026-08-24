package com.contractdrift.domain;

/**
 * Impact level of a detected change.
 *
 * <p>
 * {@code BREAKING} means existing API consumers may break after deployment.
 * {@code NON_BREAKING} means the change is backward-compatible.
 */
public enum Severity {

    /** Change will likely break existing consumers. */
    BREAKING,

    /** Change is backward-compatible. */
    NON_BREAKING
}
