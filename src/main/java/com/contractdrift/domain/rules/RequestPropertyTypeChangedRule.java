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
 * Detects type changes in request body properties.
 */
public class RequestPropertyTypeChangedRule implements CompatibilityRule {

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

            if (oldProp.type() != null && newProp.type() != null
                    && !oldProp.type().equals(newProp.type())) {
                changes.add(new Change(
                        ChangeType.REQUEST_PROPERTY_TYPE_CHANGED,
                        Severity.BREAKING,
                        location,
                        propName + ": " + oldProp.type(),
                        propName + ": " + newProp.type(),
                        "Type changed: " + propName + " from " + oldProp.type() + " to " + newProp.type()));
            }
        }

        return changes;
    }
}
