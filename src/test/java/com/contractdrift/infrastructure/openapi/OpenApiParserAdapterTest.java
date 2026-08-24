package com.contractdrift.infrastructure.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import com.contractdrift.domain.Contract;
import com.contractdrift.domain.Endpoint;
import com.contractdrift.domain.EndpointKey;
import com.contractdrift.domain.HttpMethod;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OpenApiParserAdapterTest {

    private static final Path BASELINE = Path.of("src/test/resources/contracts/baseline.yaml");

    private OpenApiParserAdapter parser;

    @BeforeEach
    void setUp() {
        parser = new OpenApiParserAdapter();
    }

    @Nested
    @DisplayName("Endpoints parsing")
    class EndpointsParsing {

        @Test
        @DisplayName("should parse all endpoints from the contract")
        void shouldParseAllEndpoints() {
            Contract contract = parser.parse(BASELINE);

            assertThat(contract.endpoints()).hasSize(6);
        }

        @Test
        @DisplayName("should parse all HTTP methods")
        void shouldParseAllHttpMethods() {
            Contract contract = parser.parse(BASELINE);

            assertThat(contract.endpoints().keySet())
                    .extracting(EndpointKey::method)
                    .containsExactlyInAnyOrder(
                            HttpMethod.GET, HttpMethod.GET,
                            HttpMethod.POST, HttpMethod.POST,
                            HttpMethod.PUT, HttpMethod.DELETE
                    );
        }
    }

    @Nested
    @DisplayName("Parameters parsing")
    class ParametersParsing {

        private Contract contract;

        @BeforeEach
        void setUp() {
            contract = parser.parse(BASELINE);
        }

        @Test
        @DisplayName("should parse path parameters with required flag")
        void shouldParsePathParameters() {
            Endpoint endpoint = getEndpoint(contract, "/users/{id}", HttpMethod.GET);

            assertThat(endpoint.parameters()).hasSize(1);
            assertThat(endpoint.parameters().get(0).name()).isEqualTo("id");
            assertThat(endpoint.parameters().get(0).location()).isEqualTo("path");
            assertThat(endpoint.parameters().get(0).required()).isTrue();
            assertThat(endpoint.parameters().get(0).type()).isEqualTo("string");
        }

        @Test
        @DisplayName("should parse query parameters")
        void shouldParseQueryParameters() {
            Endpoint endpoint = getEndpoint(contract, "/users", HttpMethod.GET);

            assertThat(endpoint.parameters()).hasSize(2);
            assertThat(endpoint.parameters()).extracting(p -> p.name())
                    .containsExactlyInAnyOrder("page", "status");
        }

        @Test
        @DisplayName("should return empty parameters for POST /users")
        void shouldReturnEmptyParametersWhenNone() {
            Endpoint endpoint = getEndpoint(contract, "/users", HttpMethod.POST);

            assertThat(endpoint.parameters()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Request body parsing")
    class RequestBodyParsing {

        private Contract contract;

        @BeforeEach
        void setUp() {
            contract = parser.parse(BASELINE);
        }

        @Test
        @DisplayName("should parse request body schema with properties")
        void shouldParseRequestBodyProperties() {
            Endpoint endpoint = getEndpoint(contract, "/users", HttpMethod.POST);

            assertThat(endpoint.requestBody()).isNotNull();
            assertThat(endpoint.requestBody().properties()).hasSize(3);
            assertThat(endpoint.requestBody().properties()).containsKeys("email", "name", "password");
        }

        @Test
        @DisplayName("should parse required properties from request body")
        void shouldParseRequiredProperties() {
            Endpoint endpoint = getEndpoint(contract, "/users", HttpMethod.POST);

            assertThat(endpoint.requestBody().requiredProperties())
                    .containsExactlyInAnyOrder("email", "name", "password");
        }

        @Test
        @DisplayName("should return null body for GET endpoints")
        void shouldReturnNullBodyForGet() {
            Endpoint endpoint = getEndpoint(contract, "/users/{id}", HttpMethod.GET);

            assertThat(endpoint.requestBody()).isNull();
        }
    }

    @Nested
    @DisplayName("Responses parsing")
    class ResponsesParsing {

        private Contract contract;

        @BeforeEach
        void setUp() {
            contract = parser.parse(BASELINE);
        }

        @Test
        @DisplayName("should parse response status codes")
        void shouldParseResponseStatusCodes() {
            Endpoint endpoint = getEndpoint(contract, "/users/{id}", HttpMethod.GET);

            assertThat(endpoint.responses()).containsKey("200");
        }

        @Test
        @DisplayName("should parse response schema properties")
        void shouldParseResponseProperties() {
            Endpoint endpoint = getEndpoint(contract, "/users/{id}", HttpMethod.GET);

            assertThat(endpoint.responses().get("200").properties()).hasSize(6);
            assertThat(endpoint.responses().get("200").properties())
                    .containsKeys("id", "email", "name", "role", "status", "created_at");
        }

        @Test
        @DisplayName("should parse response property types")
        void shouldParseResponsePropertyTypes() {
            Endpoint endpoint = getEndpoint(contract, "/users/{id}", HttpMethod.GET);

            assertThat(endpoint.responses().get("200").properties().get("id").type())
                    .isEqualTo("string");
            assertThat(endpoint.responses().get("200").properties().get("email").type())
                    .isEqualTo("string");
        }
    }

    // ── Helpers ──────────────────────────────────────────────

    private static Endpoint getEndpoint(Contract contract, String path, HttpMethod method) {
        return contract.endpoints().get(new EndpointKey(path, method));
    }
}
