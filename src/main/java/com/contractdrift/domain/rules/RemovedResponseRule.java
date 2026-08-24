package com.contractdrift.domain.rules;

import java.util.ArrayList;
import java.util.List;

import com.contractdrift.domain.Change;
import com.contractdrift.domain.ChangeType;
import com.contractdrift.domain.CompatibilityRule;
import com.contractdrift.domain.Endpoint;
import com.contractdrift.domain.Severity;

/**
 * Detects response status codes that were removed from an endpoint.
 */
public class RemovedResponseRule implements CompatibilityRule {

    @Override
    public List<Change> apply(Endpoint oldEndpoint, Endpoint newEndpoint, String location) {
        List<Change> changes = new ArrayList<>();

        for (String statusCode : oldEndpoint.responses().keySet()) {
            if (!newEndpoint.responses().containsKey(statusCode)) {
                changes.add(new Change(
                        ChangeType.RESPONSE_REMOVED,
                        Severity.BREAKING,
                        location,
                        statusCode,
                        "missing",
                        "Response removed: " + statusCode));
            }
        }

        return changes;
    }
}
