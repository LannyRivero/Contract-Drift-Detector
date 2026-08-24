package com.contractdrift.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ContractDifferTest {

    private final ContractDiffer differ = new ContractDiffer();

    @Test
    void shouldDetectRemovedEndpoint() {
        EndpointKey usersEndpoint = new EndpointKey("/users/{id}", HttpMethod.GET);
        Contract oldContract = contractWith(usersEndpoint);
        Contract newContract = new Contract(Map.of());

        List<Change> changes = differ.diff(oldContract, newContract);

        assertThat(changes)
                .hasSize(1)
                .first()
                .satisfies(change -> {
                    assertThat(change.type()).isEqualTo(ChangeType.ENDPOINT_REMOVED);
                    assertThat(change.severity()).isEqualTo(Severity.BREAKING);
                    assertThat(change.location()).isEqualTo("GET /users/{id}");
                    assertThat(change.message()).isEqualTo("Endpoint removed");
                });
    }

    @Test
    void shouldNotFlagNewEndpointAsBreaking() {
        EndpointKey usersEndpoint = new EndpointKey("/users", HttpMethod.GET);
        Contract oldContract = new Contract(Map.of());
        Contract newContract = contractWith(usersEndpoint);

        List<Change> changes = differ.diff(oldContract, newContract);

        assertThat(changes).isEmpty();
    }

    private static Contract contractWith(EndpointKey endpointKey) {
        Endpoint endpoint = new Endpoint(endpointKey, List.of(), null, Map.of());
        return new Contract(Map.of(endpointKey, endpoint));
    }
}
