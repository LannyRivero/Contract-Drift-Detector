package com.contractdrift.domain.rules;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.contractdrift.domain.*;

class ParameterRulesTest {

    private final DiffEngine engine = new DiffEngine();
    private final EndpointKey key = endpointKey("/users", HttpMethod.GET);

    @Test
    @DisplayName("should detect a parameter that was removed")
    void shouldDetectRemovedParameter() {
        ApiParameter oldParam = pathParam("id", true);
        Contract oldContract = contractWith(key, List.of(oldParam));
        Contract newContract = contractWith(key, List.of());

        List<Change> changes = engine.diff(oldContract, newContract);

        assertThat(changes)
                .hasSize(1)
                .first()
                .satisfies(change -> {
                    assertThat(change.type()).isEqualTo(ChangeType.PARAMETER_REMOVED);
                    assertThat(change.severity()).isEqualTo(Severity.BREAKING);
                    assertThat(change.message()).contains("id");
                });
    }

    @ParameterizedTest(name = "{0}: {1} from {2} to {3} → {4}")
    @DisplayName("should detect parameter requirement change")
    @CsvSource({
            "filter, query, false, true,  PARAMETER_BECAME_REQUIRED",
            "token,  header, false, true,  PARAMETER_BECAME_REQUIRED",
            "limit,  query,  false, false, _NONE"
    })
    void shouldDetectParameterRequirementChange(
            String name, String location,
            boolean oldRequired, boolean newRequired,
            String expectedType
    ) {
        ApiParameter oldParam = new ApiParameter(name, location, oldRequired, "string");
        ApiParameter newParam = new ApiParameter(name, location, newRequired, "string");
        Contract oldContract = contractWith(key, List.of(oldParam));
        Contract newContract = contractWith(key, List.of(newParam));

        List<Change> changes = engine.diff(oldContract, newContract);

        if ("_NONE".equals(expectedType)) {
            assertThat(changes).isEmpty();
        } else {
            assertThat(changes)
                    .hasSize(1)
                    .first()
                    .satisfies(change -> {
                        assertThat(change.type()).isEqualTo(ChangeType.valueOf(expectedType));
                        assertThat(change.severity()).isEqualTo(Severity.BREAKING);
                        assertThat(change.message()).contains(name);
                    });
        }
    }

    // ── Helpers ──────────────────────────────────────────────

    private static EndpointKey endpointKey(String path, HttpMethod method) {
        return new EndpointKey(path, method);
    }

    private static Contract contractWith(EndpointKey key, List<ApiParameter> params) {
        Endpoint endpoint = new Endpoint(key, params, null, Map.of());
        return new Contract(Map.of(key, endpoint));
    }

    private static ApiParameter pathParam(String name, boolean required) {
        return new ApiParameter(name, "path", required, "string");
    }
}
