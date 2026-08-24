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
 * Detects optional parameters that became required.
 */
public class ParameterBecameRequiredRule implements CompatibilityRule {

    @Override
    public List<Change> apply(Endpoint oldEndpoint, Endpoint newEndpoint, String location) {
        List<Change> changes = new ArrayList<>();

        for (ApiParameter oldParam : oldEndpoint.parameters()) {
            newEndpoint.parameters().stream()
                    .filter(p -> p.name().equals(oldParam.name())
                            && p.location().equals(oldParam.location()))
                    .findFirst()
                    .ifPresent(newParam -> {
                        if (!oldParam.required() && newParam.required()) {
                            changes.add(new Change(
                                    ChangeType.PARAMETER_BECAME_REQUIRED,
                                    Severity.BREAKING,
                                    location,
                                    oldParam.name() + " (optional)",
                                    oldParam.name() + " (required)",
                                    "Parameter became required: " + oldParam.name()));
                        }
                    });
        }

        return changes;
    }
}
