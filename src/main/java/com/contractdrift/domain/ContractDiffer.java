package com.contractdrift.domain;

import java.util.ArrayList;
import java.util.List;

public class ContractDiffer {

    public List<Change> diff(Contract oldContract, Contract newContract) {
        List<Change> changes = new ArrayList<>();

        detectRemovedEndpoints(oldContract, newContract, changes);

        return changes;
    }

    private void detectRemovedEndpoints(
            Contract oldContract,
            Contract newContract,
            List<Change> changes
    ) {
        for (EndpointKey oldEndpoint : oldContract.endpoints().keySet()) {
            if (!newContract.endpoints().containsKey(oldEndpoint)) {
                changes.add(new Change(
                        ChangeType.ENDPOINT_REMOVED,
                        Severity.BREAKING,
                        oldEndpoint.displayName(),
                        "present",
                        "missing",
                        "Endpoint removed"
                ));
            }
        }
    }
}
