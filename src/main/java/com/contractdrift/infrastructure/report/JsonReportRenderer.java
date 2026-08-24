package com.contractdrift.infrastructure.report;

import java.util.List;

import com.contractdrift.domain.Change;
import com.contractdrift.domain.Severity;

/**
 * Adapter that renders a list of {@link Change} objects into JSON format.
 *
 * <p>
 * This is an <strong>output adapter</strong> in hexagonal architecture.
 * The JSON output is designed for CI/CD integration and programmatic
 * consumption.
 *
 * <p>
 * Output format:
 * 
 * <pre>
 * {
 *   "summary": {
 *     "breaking": 2,
 *     "safe": 1,
 *     "total": 3
 *   },
 *   "hasBreakingChanges": true,
 *   "changes": [
 *     {
 *       "type": "ENDPOINT_REMOVED",
 *       "severity": "BREAKING",
 *       "location": "GET /users/{id}",
 *       "message": "Endpoint removed"
 *     }
 *   ]
 * }
 * </pre>
 */
public class JsonReportRenderer {

    /**
     * Renders changes into a JSON string.
     *
     * @param changes list of detected changes
     * @return JSON string
     */
    public String render(List<Change> changes) {
        long breakingCount = changes.stream()
                .filter(c -> c.severity() == Severity.BREAKING)
                .count();
        long safeCount = changes.stream()
                .filter(c -> c.severity() == Severity.NON_BREAKING)
                .count();

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        // Summary
        sb.append("  \"summary\": {\n");
        sb.append("    \"breaking\": ").append(breakingCount).append(",\n");
        sb.append("    \"safe\": ").append(safeCount).append(",\n");
        sb.append("    \"total\": ").append(changes.size()).append("\n");
        sb.append("  },\n");

        // Has breaking changes flag
        sb.append("  \"hasBreakingChanges\": ").append(breakingCount > 0).append(",\n");

        // Changes array
        sb.append("  \"changes\": [");
        for (int i = 0; i < changes.size(); i++) {
            Change change = changes.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\n    {\n");
            sb.append("      \"type\": \"").append(escapeJson(change.type().name())).append("\",\n");
            sb.append("      \"severity\": \"").append(escapeJson(change.severity().name())).append("\",\n");
            sb.append("      \"location\": \"").append(escapeJson(change.location())).append("\",\n");
            sb.append("      \"oldValue\": \"").append(escapeJson(change.oldValue())).append("\",\n");
            sb.append("      \"newValue\": \"").append(escapeJson(change.newValue())).append("\",\n");
            sb.append("      \"message\": \"").append(escapeJson(change.message())).append("\"\n");
            sb.append("    }");
        }
        if (!changes.isEmpty()) {
            sb.append("\n");
        }
        sb.append("  ]\n");

        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
