package com.contractdrift.domain.rules;

import java.util.ArrayList;
import java.util.List;

import com.contractdrift.domain.Change;
import com.contractdrift.domain.ChangeType;
import com.contractdrift.domain.Contract;
import com.contractdrift.domain.EndpointKey;
import com.contractdrift.domain.Severity;

/**
 * Detects endpoints that were removed from the new contract.
 *
 * <p>
 * This rule operates at the {@link Contract} level (not per-endpoint)
 * because it compares the set of endpoints across contracts.
 */
public class RemovedEndpointRule {

    /**
     * Detects endpoints present in the old contract but missing in the new one.
     *
     * @param oldContract previous contract
     * @param newContract new contract
     * @return list of removed endpoints
     */
    public List<Change> apply(Contract oldContract, Contract newContract) {
        List<Change> changes = new ArrayList<>();

        for (EndpointKey key : oldContract.endpoints().keySet()) {
            if (!newContract.endpoints().containsKey(key)) {
                changes.add(new Change(
                        ChangeType.ENDPOINT_REMOVED,
                        Severity.BREAKING,
                        key.displayName(),
                        "present",
                        "missing",
                        "Endpoint removed"));
            }
        }

        return changes;
    }
}
