package com.contractdrift.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DiffEngineTest {

    private final DiffEngine engine = new DiffEngine();

    @Test
    @DisplayName("should detect an endpoint that was removed from the new contract")
    void shouldDetectRemovedEndpoint() {
        EndpointKey key = endpointKey("/users/{id}", HttpMethod.GET);
        Contract oldContract = contractWith(key);
        Contract newContract = emptyContract();

        List<Change> changes = engine.diff(oldContract, newContract);

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
    @DisplayName("should not flag a newly added endpoint as breaking")
    void shouldNotFlagNewEndpointAsBreaking() {
        EndpointKey key = endpointKey("/users", HttpMethod.GET);
        Contract oldContract = emptyContract();
        Contract newContract = contractWith(key);

        List<Change> changes = engine.diff(oldContract, newContract);

        assertThat(changes).isEmpty();
    }

    // ── Helpers ──────────────────────────────────────────────

    private static EndpointKey endpointKey(String path, HttpMethod method) {
        return new EndpointKey(path, method);
    }

    private static Contract emptyContract() {
        return new Contract(Map.of());
    }

    private static Contract contractWith(EndpointKey key) {
        Endpoint endpoint = new Endpoint(key, List.of(), null, Map.of());
        return new Contract(Map.of(key, endpoint));
    }
}
