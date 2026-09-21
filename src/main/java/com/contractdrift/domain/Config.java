package com.contractdrift.domain;

import java.util.Set;

/**
 * Configuration for the diff engine.
 *
 * <p>
 * Allows customizing which rules are enabled and their behavior.
 *
 * @param ignoredRules  set of rule types to ignore (e.g., "PARAMETER_REMOVED")
 * @param failOnWarning if true, exit with code 1 even for non-breaking changes
 */
public record Config(
        Set<String> ignoredRules,
        boolean failOnWarning) {
    /**
     * Default configuration with all rules enabled.
     */
    public static Config defaultConfig() {
        return new Config(Set.of(), false);
    }

    /**
     * Checks if a rule type is enabled.
     *
     * @param ruleType the rule type to check
     * @return true if the rule is enabled (not ignored)
     */
    public boolean isRuleEnabled(String ruleType) {
        return !ignoredRules.contains(ruleType);
    }
}
