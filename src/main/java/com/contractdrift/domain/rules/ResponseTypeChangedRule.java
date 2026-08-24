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
 * Detects type changes in response properties.
 */
public class ResponseTypeChangedRule implements CompatibilityRule {

    @Override
    public List<Change> apply(Endpoint oldEndpoint, Endpoint newEndpoint, String location) {
        List<Change> changes = new ArrayList<>();

        for (String statusCode : oldEndpoint.responses().keySet()) {
            if (!newEndpoint.responses().containsKey(statusCode)) {
                continue;
            }

            ApiSchema oldSchema = oldEndpoint.responses().get(statusCode);
            ApiSchema newSchema = newEndpoint.responses().get(statusCode);

            if (oldSchema == null || newSchema == null) {
                continue;
            }

            for (String propName : oldSchema.properties().keySet()) {
                if (!newSchema.properties().containsKey(propName)) {
                    continue;
                }

                String oldType = oldSchema.properties().get(propName).type();
                String newType = newSchema.properties().get(propName).type();

                if (oldType != null && newType != null && !oldType.equals(newType)) {
                    changes.add(new Change(
                            ChangeType.RESPONSE_PROPERTY_TYPE_CHANGED,
                            Severity.BREAKING,
                            location + " [" + statusCode + "]",
                            propName + ": " + oldType,
                            propName + ": " + newType,
                            "Type changed: " + propName + " from " + oldType + " to " + newType));
                }
            }
        }

        return changes;
    }
}
