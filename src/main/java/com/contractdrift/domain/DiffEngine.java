package com.contractdrift.domain;

import java.util.ArrayList;
import java.util.List;

import com.contractdrift.domain.rules.AddedRequiredParameterRule;
import com.contractdrift.domain.rules.ParameterBecameRequiredRule;
import com.contractdrift.domain.rules.RemovedEndpointRule;
import com.contractdrift.domain.rules.RemovedParameterRule;
import com.contractdrift.domain.rules.RemovedRequestPropertyRule;
import com.contractdrift.domain.rules.RemovedResponseRule;
import com.contractdrift.domain.rules.RequestPropertyBecameRequiredRule;
import com.contractdrift.domain.rules.RequestPropertyTypeChangedRule;
import com.contractdrift.domain.rules.ResponsePropertyRemovedRule;
import com.contractdrift.domain.rules.ResponseTypeChangedRule;

/**
 * Domain service that orchestrates all compatibility rules.
 *
 * <p>
 * Follows the <strong>Open/Closed Principle</strong>: adding a new rule
 * means creating a new class — not modifying this engine. The engine
 * simply iterates over registered rules and collects their results.
 *
 * <p>
 * Architecture:
 * 
 * <pre>
 *   Contract v1 ─┐
 *                ├── DiffEngine
 *   Contract v2 ─┘       │
 *                        ├── RemovedEndpointRule
 *                        ├── RemovedParameterRule
 *                        ├── ParameterBecameRequiredRule
 *                        ├── RemovedResponseRule
 *                        ├── ResponsePropertyRemovedRule
 *                        └── ResponseTypeChangedRule
 *                                ↓
 *                          List&lt;Change&gt;
 * </pre>
 */
public class DiffEngine {

    private final RemovedEndpointRule removedEndpointRule = new RemovedEndpointRule();
    private final List<CompatibilityRule> endpointRules = List.of(
            new RemovedParameterRule(),
            new AddedRequiredParameterRule(),
            new ParameterBecameRequiredRule(),
            new RemovedRequestPropertyRule(),
            new RequestPropertyBecameRequiredRule(),
            new RequestPropertyTypeChangedRule(),
            new RemovedResponseRule(),
            new ResponsePropertyRemovedRule(),
            new ResponseTypeChangedRule());

    /**
     * Compares two contracts using default config.
     *
     * @param oldContract the previous version of the API contract
     * @param newContract the new version of the API contract
     * @return list of changes found (empty if contracts are identical)
     */
    public List<Change> diff(Contract oldContract, Contract newContract) {
        return diff(oldContract, newContract, Config.defaultConfig());
    }

    /**
     * Compares two contracts using custom config.
     *
     * @param oldContract the previous version of the API contract
     * @param newContract the new version of the API contract
     * @param config      configuration for the diff engine
     * @return list of changes found (empty if contracts are identical)
     */
    public List<Change> diff(Contract oldContract, Contract newContract, Config config) {
        List<Change> changes = new ArrayList<>();

        List<Change> endpointRemovals = removedEndpointRule.apply(oldContract, newContract);
        changes.addAll(filterByConfig(endpointRemovals, config));

        for (EndpointKey key : oldContract.endpoints().keySet()) {
            if (newContract.endpoints().containsKey(key)) {
                Endpoint oldEndpoint = oldContract.endpoints().get(key);
                Endpoint newEndpoint = newContract.endpoints().get(key);
                String location = key.displayName();

                for (CompatibilityRule rule : endpointRules) {
                    List<Change> ruleChanges = rule.apply(oldEndpoint, newEndpoint, location);
                    changes.addAll(filterByConfig(ruleChanges, config));
                }
            }
        }

        return changes;
    }

    private List<Change> filterByConfig(List<Change> changes, Config config) {
        if (config.ignoredRules().isEmpty()) {
            return changes;
        }
        return changes.stream()
                .filter(change -> config.isRuleEnabled(change.type().name()))
                .toList();
    }
}
