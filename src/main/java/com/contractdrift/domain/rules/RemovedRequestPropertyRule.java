package com.contractdrift.domain.rules;

import java.util.ArrayList;
import java.util.List;

import com.contractdrift.domain.ApiSchema;
import com.contractdrift.domain.Change;
import com.contractdrift.domain.ChangeType;
import com.contractdrift.domain.CompatibilityRule;
import com.contractdrift.domain.Endpoint;
import com.contractdrift.domain.Severity;

/**
 * Detects properties removed from the request body schema.
 */
public class RemovedRequestPropertyRule implements CompatibilityRule {

    @Override
    public List<Change> apply(Endpoint oldEndpoint, Endpoint newEndpoint, String location) {
        List<Change> changes = new ArrayList<>();

        ApiSchema oldBody = oldEndpoint.requestBody();
        ApiSchema newBody = newEndpoint.requestBody();

        if (oldBody == null || newBody == null) {
            return changes;
        }

        for (String propName : oldBody.properties().keySet()) {
            if (!newBody.properties().containsKey(propName)) {
                changes.add(new Change(
                        ChangeType.REQUEST_PROPERTY_REMOVED,
                        Severity.BREAKING,
                        location,
                        propName,
                        "missing",
                        "Request property removed: " + propName
                ));
            }
        }

        return changes;
    }
}
