package com.contractdrift.infrastructure.report;

import java.util.List;

import com.contractdrift.domain.Change;
import com.contractdrift.domain.Severity;

/**
 * Adapter that renders a list of {@link Change} objects into a
 * human-readable console report.
 *
 * <p>
 * This is an <strong>output adapter</strong> in hexagonal architecture.
 * Future versions may add JSON, Markdown, or GitHub PR comment renderers.
 */
public class ConsoleReportRenderer {

    /**
     * Renders changes into a formatted text report.
     *
     * @param changes list of detected changes
     * @return formatted report string
     */
    public String render(List<Change> changes) {
        long breakingCount = changes.stream()
                .filter(c -> c.severity() == Severity.BREAKING)
                .count();
        long safeCount = changes.stream()
                .filter(c -> c.severity() == Severity.NON_BREAKING)
                .count();

        StringBuilder sb = new StringBuilder();
        sb.append("=================================\n");
        sb.append("API CONTRACT COMPATIBILITY REPORT\n");
        sb.append("=================================\n\n");
        sb.append("Breaking changes: ").append(breakingCount).append("\n");
        sb.append("Safe changes: ").append(safeCount).append("\n\n");

        if (!changes.isEmpty()) {
            sb.append("[BREAKING]\n");
            changes.stream()
                    .filter(c -> c.severity() == Severity.BREAKING)
                    .forEach(change -> {
                        sb.append(change.location()).append("\n");
                        sb.append(change.message()).append("\n\n");
                    });

            sb.append("[SAFE]\n");
            changes.stream()
                    .filter(c -> c.severity() == Severity.NON_BREAKING)
                    .forEach(change -> {
                        sb.append(change.location()).append("\n");
                        sb.append(change.message()).append("\n\n");
                    });
        } else {
            sb.append("No changes detected.\n");
        }

        return sb.toString();
    }
}
