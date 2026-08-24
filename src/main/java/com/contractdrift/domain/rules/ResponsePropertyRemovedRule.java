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
 * Detects properties removed from response schemas.
 */
public class ResponsePropertyRemovedRule implements CompatibilityRule {

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
                    changes.add(new Change(
                            ChangeType.RESPONSE_PROPERTY_REMOVED,
                            Severity.BREAKING,
                            location + " [" + statusCode + "]",
                            propName,
                            "missing",
                            "Response property removed: " + propName));
                }
            }
        }

        return changes;
    }
}
