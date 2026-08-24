package com.contractdrift.domain.rules;

import java.util.ArrayList;
import java.util.List;

import com.contractdrift.domain.ApiParameter;
import com.contractdrift.domain.Change;
import com.contractdrift.domain.ChangeType;
import com.contractdrift.domain.CompatibilityRule;
import com.contractdrift.domain.Endpoint;
import com.contractdrift.domain.Severity;

/**
 * Detects parameters that were removed from an endpoint.
 */
public class RemovedParameterRule implements CompatibilityRule {

    @Override
    public List<Change> apply(Endpoint oldEndpoint, Endpoint newEndpoint, String location) {
        List<Change> changes = new ArrayList<>();

        for (ApiParameter oldParam : oldEndpoint.parameters()) {
            boolean found = newEndpoint.parameters().stream()
                    .anyMatch(p -> p.name().equals(oldParam.name())
                            && p.location().equals(oldParam.location()));

            if (!found) {
                changes.add(new Change(
                        ChangeType.PARAMETER_REMOVED,
                        Severity.BREAKING,
                        location,
                        oldParam.name(),
                        "missing",
                        "Parameter removed: " + oldParam.name()));
            }
        }

        return changes;
    }
}
