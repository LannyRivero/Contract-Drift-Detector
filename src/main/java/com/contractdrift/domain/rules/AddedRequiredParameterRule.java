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
 * Detects when an existing endpoint gains a new required parameter.
 *
 * <p>
 * This is breaking because existing clients don't send the new parameter.
 *
 * <p>
 * Example:
 * 
 * <pre>
 *   v1: GET /users
 *   v2: GET /users?token=xxx (token required)
 * </pre>
 */
public class AddedRequiredParameterRule implements CompatibilityRule {

    @Override
    public List<Change> apply(Endpoint oldEndpoint, Endpoint newEndpoint, String location) {
        List<Change> changes = new ArrayList<>();

        for (ApiParameter newParam : newEndpoint.parameters()) {
            if (newParam.required()) {
                boolean existedInOld = oldEndpoint.parameters().stream()
                        .anyMatch(p -> p.name().equals(newParam.name()));

                if (!existedInOld) {
                    changes.add(new Change(
                            ChangeType.PARAMETER_BECAME_REQUIRED,
                            Severity.BREAKING,
                            location,
                            newParam.name(),
                            newParam.name() + " (required)",
                            "New required parameter: " + newParam.name()));
                }
            }
        }

        return changes;
    }
}
