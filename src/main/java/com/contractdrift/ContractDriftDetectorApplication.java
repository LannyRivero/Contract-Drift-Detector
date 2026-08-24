package com.contractdrift;

import java.nio.file.Path;
import java.util.List;

import com.contractdrift.application.CompareContractsUseCase;
import com.contractdrift.domain.Change;
import com.contractdrift.domain.Severity;
import com.contractdrift.infrastructure.openapi.OpenApiParserAdapter;
import com.contractdrift.infrastructure.report.ConsoleReportRenderer;

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
 *   contract-drift-detector &lt;old-contract.yaml&gt; &lt;new-contract.yaml&gt;
 * </pre>
 */
public class ContractDriftDetectorApplication {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: contract-drift-detector <old-contract.yaml> <new-contract.yaml>");
            System.exit(1);
        }

        Path oldContract = Path.of(args[0]);
        Path newContract = Path.of(args[1]);

        CompareContractsUseCase useCase = new CompareContractsUseCase(new OpenApiParserAdapter());
        ConsoleReportRenderer renderer = new ConsoleReportRenderer();

        List<Change> changes = useCase.execute(oldContract, newContract);
        String report = renderer.render(changes);

        System.out.println(report);

        boolean hasBreaking = changes.stream()
                .anyMatch(c -> c.severity() == Severity.BREAKING);
        if (hasBreaking) {
            System.exit(1);
        }
    }
}
