package com.contractdrift.domain.rules;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.contractdrift.domain.*;

class ResponseRulesTest {

    private final DiffEngine engine = new DiffEngine();
    private final EndpointKey key = endpointKey("/users", HttpMethod.GET);

    @Test
    @DisplayName("should detect a response status code that was removed")
    void shouldDetectRemovedResponse() {
        ApiSchema schema = schemaWith(Map.of());
        Contract oldContract = contractWithResponses(key, Map.of("200", schema, "404", schema));
        Contract newContract = contractWithResponses(key, Map.of("200", schema));

        List<Change> changes = engine.diff(oldContract, newContract);

        assertThat(changes)
                .anySatisfy(change -> {
                    assertThat(change.type()).isEqualTo(ChangeType.RESPONSE_REMOVED);
                    assertThat(change.severity()).isEqualTo(Severity.BREAKING);
                    assertThat(change.message()).contains("404");
                });
    }

    @Test
    @DisplayName("should detect a property removed from response schema")
    void shouldDetectResponsePropertyRemoved() {
        ApiSchema oldSchema = schemaWith(Map.of(
                "id", property("id", "string"),
                "name", property("name", "string")
        ));
        ApiSchema newSchema = schemaWith(Map.of(
                "id", property("id", "string")
        ));
        Contract oldContract = contractWithResponses(key, Map.of("200", oldSchema));
        Contract newContract = contractWithResponses(key, Map.of("200", newSchema));

        List<Change> changes = engine.diff(oldContract, newContract);

        assertThat(changes)
                .anySatisfy(change -> {
                    assertThat(change.type()).isEqualTo(ChangeType.RESPONSE_PROPERTY_REMOVED);
                    assertThat(change.severity()).isEqualTo(Severity.BREAKING);
                    assertThat(change.message()).contains("name");
                });
    }

    @ParameterizedTest(name = "{0}: {1} → {2} = {3}")
    @DisplayName("should detect response property type change")
    @CsvSource({
            "email, string, integer, RESPONSE_PROPERTY_TYPE_CHANGED",
            "id,    string, string,  _NONE",
            "age,   integer, boolean, RESPONSE_PROPERTY_TYPE_CHANGED"
    })
    void shouldDetectResponseTypeChange(
            String propName, String oldType, String newType, String expectedChangeType
    ) {
        ApiSchema oldSchema = schemaWith(Map.of(propName, property(propName, oldType)));
        ApiSchema newSchema = schemaWith(Map.of(propName, property(propName, newType)));
        Contract oldContract = contractWithResponses(key, Map.of("200", oldSchema));
        Contract newContract = contractWithResponses(key, Map.of("200", newSchema));

        List<Change> changes = engine.diff(oldContract, newContract);

        if ("_NONE".equals(expectedChangeType)) {
            assertThat(changes).isEmpty();
        } else {
            assertThat(changes)
                    .anySatisfy(change -> {
                        assertThat(change.type()).isEqualTo(ChangeType.valueOf(expectedChangeType));
                        assertThat(change.severity()).isEqualTo(Severity.BREAKING);
                        assertThat(change.message()).contains(oldType).contains(newType);
                    });
        }
    }

    // ── Helpers ──────────────────────────────────────────────

    private static EndpointKey endpointKey(String path, HttpMethod method) {
        return new EndpointKey(path, method);
    }

    private static Contract contractWithResponses(EndpointKey key, Map<String, ApiSchema> responses) {
        Endpoint endpoint = new Endpoint(key, List.of(), null, responses);
        return new Contract(Map.of(key, endpoint));
    }

    private static ApiSchema schemaWith(Map<String, ApiProperty> properties) {
        return new ApiSchema(null, "object", properties, Set.of());
    }

    private static ApiProperty property(String name, String type) {
        return new ApiProperty(name, type, true);
    }
}
