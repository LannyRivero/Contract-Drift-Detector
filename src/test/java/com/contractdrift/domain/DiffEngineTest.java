package com.contractdrift.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DiffEngineTest {

    private final DiffEngine engine = new DiffEngine();

    @Nested
    @DisplayName("Removed Endpoint")
    class RemovedEndpoint {

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
    }

    @Nested
    @DisplayName("Parameter Rules")
    class ParameterRules {

        @Test
        @DisplayName("should detect a parameter that was removed")
        void shouldDetectRemovedParameter() {
            EndpointKey key = endpointKey("/users", HttpMethod.GET);
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
            EndpointKey key = endpointKey("/users", HttpMethod.GET);
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
    }

    @Nested
    @DisplayName("Response Rules")
    class ResponseRules {

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
    }

    @Nested
    @DisplayName("Request Body Rules")
    class RequestBodyRules {

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
    }

    // ── Helpers ──────────────────────────────────────────────

    private static EndpointKey endpointKey(String path, HttpMethod method) {
        return new EndpointKey(path, method);
    }

    private static Contract emptyContract() {
        return new Contract(Map.of());
    }

    private static Contract contractWith(EndpointKey key) {
        return contractWith(key, List.of());
    }

    private static Contract contractWith(EndpointKey key, List<ApiParameter> params) {
        Endpoint endpoint = new Endpoint(key, params, null, Map.of());
        return new Contract(Map.of(key, endpoint));
    }

    private static Contract contractWithResponses(EndpointKey key, Map<String, ApiSchema> responses) {
        Endpoint endpoint = new Endpoint(key, List.of(), null, responses);
        return new Contract(Map.of(key, endpoint));
    }

    private static Contract contractWithBody(EndpointKey key, ApiSchema body) {
        Endpoint endpoint = new Endpoint(key, List.of(), body, Map.of());
        return new Contract(Map.of(key, endpoint));
    }

    private static ApiParameter pathParam(String name, boolean required) {
        return new ApiParameter(name, "path", required, "string");
    }

    private static ApiSchema schemaWith(Map<String, ApiProperty> properties) {
        return new ApiSchema(null, "object", properties, Set.of());
    }

    private static ApiProperty property(String name, String type) {
        return new ApiProperty(name, type, true);
    }
}
