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

class RequestBodyRulesTest {

    private final DiffEngine engine = new DiffEngine();
    private final EndpointKey key = endpointKey("/users", HttpMethod.POST);

    @Test
    @DisplayName("should detect a property removed from request body")
    void shouldDetectRemovedRequestProperty() {
        ApiSchema oldBody = schemaWith(Map.of(
                "email", property("email", "string"),
                "name", property("name", "string")
        ));
        ApiSchema newBody = schemaWith(Map.of(
                "email", property("email", "string")
        ));
        Contract oldContract = contractWithBody(key, oldBody);
        Contract newContract = contractWithBody(key, newBody);

        List<Change> changes = engine.diff(oldContract, newContract);

        assertThat(changes)
                .anySatisfy(change -> {
                    assertThat(change.type()).isEqualTo(ChangeType.REQUEST_PROPERTY_REMOVED);
                    assertThat(change.severity()).isEqualTo(Severity.BREAKING);
                    assertThat(change.message()).contains("name");
                });
    }

    @Test
    @DisplayName("should detect an optional request property that became required")
    void shouldDetectRequestPropertyBecameRequired() {
        ApiSchema oldBody = schemaWith(Map.of(
                "email", new ApiProperty("email", "string", false)
        ));
        ApiSchema newBody = schemaWith(Map.of(
                "email", new ApiProperty("email", "string", true)
        ));
        Contract oldContract = contractWithBody(key, oldBody);
        Contract newContract = contractWithBody(key, newBody);

        List<Change> changes = engine.diff(oldContract, newContract);

        assertThat(changes)
                .anySatisfy(change -> {
                    assertThat(change.type()).isEqualTo(ChangeType.REQUEST_PROPERTY_BECAME_REQUIRED);
                    assertThat(change.severity()).isEqualTo(Severity.BREAKING);
                    assertThat(change.message()).contains("email");
                });
    }

    @ParameterizedTest(name = "{0}: {1} → {2} = {3}")
    @DisplayName("should detect request property type change")
    @CsvSource({
            "age,    string,  integer, REQUEST_PROPERTY_TYPE_CHANGED",
            "email,  string,  string,  _NONE",
            "count,  integer, string,  REQUEST_PROPERTY_TYPE_CHANGED"
    })
    void shouldDetectRequestPropertyTypeChange(
            String propName, String oldType, String newType, String expectedChangeType
    ) {
        ApiSchema oldBody = schemaWith(Map.of(propName, property(propName, oldType)));
        ApiSchema newBody = schemaWith(Map.of(propName, property(propName, newType)));
        Contract oldContract = contractWithBody(key, oldBody);
        Contract newContract = contractWithBody(key, newBody);

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

    private static Contract contractWithBody(EndpointKey key, ApiSchema body) {
        Endpoint endpoint = new Endpoint(key, List.of(), body, Map.of());
        return new Contract(Map.of(key, endpoint));
    }

    private static ApiSchema schemaWith(Map<String, ApiProperty> properties) {
        return new ApiSchema(null, "object", properties, Set.of());
    }

    private static ApiProperty property(String name, String type) {
        return new ApiProperty(name, type, true);
    }
}
