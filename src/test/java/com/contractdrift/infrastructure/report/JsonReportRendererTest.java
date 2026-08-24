package com.contractdrift.infrastructure.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.contractdrift.domain.Change;
import com.contractdrift.domain.ChangeType;
import com.contractdrift.domain.Severity;

class JsonReportRendererTest {

        private final JsonReportRenderer renderer = new JsonReportRenderer();

    @Test
    @DisplayName("should render empty changes list")
    void shouldRenderEmptyList() {
        String json = renderer.render(List.of());

        assertThat(json)
                .contains("\"breaking\": 0")
                .contains("\"safe\": 0")
                .contains("\"total\": 0")
                .contains("\"hasBreakingChanges\": false")
                .contains("\"changes\":");
    }

        @Test
        @DisplayName("should render single breaking change")
        void shouldRenderSingleBreakingChange() {
                Change change = new Change(
                                ChangeType.ENDPOINT_REMOVED,
                                Severity.BREAKING,
                                "GET /users/{id}",
                                "present",
                                "missing",
                                "Endpoint removed");

                String json = renderer.render(List.of(change));

                assertThat(json)
                                .contains("\"breaking\": 1")
                                .contains("\"hasBreakingChanges\": true")
                                .contains("\"type\": \"ENDPOINT_REMOVED\"")
                                .contains("\"severity\": \"BREAKING\"")
                                .contains("\"location\": \"GET /users/{id}\"")
                                .contains("\"message\": \"Endpoint removed\"");
        }

        @Test
        @DisplayName("should render mixed breaking and safe changes")
        void shouldRenderMixedChanges() {
                Change breaking = new Change(
                                ChangeType.PARAMETER_REMOVED,
                                Severity.BREAKING,
                                "GET /users",
                                "token",
                                "missing",
                                "Parameter removed: token");
                Change safe = new Change(
                                ChangeType.PARAMETER_BECAME_REQUIRED,
                                Severity.NON_BREAKING,
                                "POST /users",
                                "limit",
                                "limit",
                                "Parameter became required: limit");

                String json = renderer.render(List.of(breaking, safe));

                assertThat(json)
                                .contains("\"breaking\": 1")
                                .contains("\"safe\": 1")
                                .contains("\"total\": 2")
                                .contains("\"hasBreakingChanges\": true");
        }

        @Test
        @DisplayName("should escape special characters in JSON")
        void shouldEscapeSpecialCharacters() {
                Change change = new Change(
                                ChangeType.ENDPOINT_REMOVED,
                                Severity.BREAKING,
                                "GET /users/{id}",
                                "present",
                                "missing",
                                "Line1\nLine2\tTabbed \"quoted\"");

                String json = renderer.render(List.of(change));

                assertThat(json)
                                .contains("Line1\\nLine2\\tTabbed \\\"quoted\\\"");
        }
}
