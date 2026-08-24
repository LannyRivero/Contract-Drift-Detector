package com.contractdrift.domain.rules;

import java.util.ArrayList;
import java.util.List;

import com.contractdrift.domain.ApiProperty;
import com.contractdrift.domain.ApiSchema;
import com.contractdrift.domain.Change;
import com.contractdrift.domain.ChangeType;
import com.contractdrift.domain.CompatibilityRule;
import com.contractdrift.domain.Endpoint;
import com.contractdrift.domain.Severity;

/**
 * Detects optional request body properties that became required.
 */
public class RequestPropertyBecameRequiredRule implements CompatibilityRule {

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
                continue;
            }

            ApiProperty oldProp = oldBody.properties().get(propName);
            ApiProperty newProp = newBody.properties().get(propName);

            if (!oldProp.required() && newProp.required()) {
                changes.add(new Change(
                        ChangeType.REQUEST_PROPERTY_BECAME_REQUIRED,
                        Severity.BREAKING,
                        location,
                        propName + " (optional)",
                        propName + " (required)",
                        "Request property became required: " + propName));
            }
        }

        return changes;
    }
}
