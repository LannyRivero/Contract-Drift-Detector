package com.contractdrift;

import java.nio.file.Path;
import java.util.List;

import com.contractdrift.application.CompareContractsUseCase;
import com.contractdrift.domain.Change;
import com.contractdrift.domain.Severity;
import com.contractdrift.infrastructure.openapi.OpenApiParserAdapter;
import com.contractdrift.infrastructure.report.ConsoleReportRenderer;
import com.contractdrift.infrastructure.report.JsonReportRenderer;

/**
 * CLI entry point for Contract Drift Detector.
 *
 * <p>
 * Compares two OpenAPI specification files and prints a compatibility report.
 * Exits with code 1 if breaking changes are found, 0 otherwise.
 *
 * <p>
 * Usage:
 * 
 * <pre>
 *   contract-drift-detector &lt;old-contract.yaml&gt; &lt;new-contract.yaml&gt; [--json]
 * </pre>
 */
public class ContractDriftDetectorApplication {

    public static void main(String[] args) {
        boolean jsonOutput = false;
        String oldContractPath = null;
        String newContractPath = null;

        for (String arg : args) {
            if ("--json".equals(arg)) {
                jsonOutput = true;
            } else if (oldContractPath == null) {
                oldContractPath = arg;
            } else if (newContractPath == null) {
                newContractPath = arg;
            }
        }

        if (oldContractPath == null || newContractPath == null) {
            System.err.println("Usage: contract-drift-detector <old-contract.yaml> <new-contract.yaml> [--json]");
            System.exit(1);
        }

        Path oldContract = Path.of(oldContractPath);
        Path newContract = Path.of(newContractPath);

        CompareContractsUseCase useCase = new CompareContractsUseCase(new OpenApiParserAdapter());

        List<Change> changes = useCase.execute(oldContract, newContract);

        String report;
        if (jsonOutput) {
            JsonReportRenderer renderer = new JsonReportRenderer();
            report = renderer.render(changes);
        } else {
            ConsoleReportRenderer renderer = new ConsoleReportRenderer();
            report = renderer.render(changes);
        }

        System.out.println(report);

        boolean hasBreaking = changes.stream()
                .anyMatch(c -> c.severity() == Severity.BREAKING);
        if (hasBreaking) {
            System.exit(1);
        }
    }
}
