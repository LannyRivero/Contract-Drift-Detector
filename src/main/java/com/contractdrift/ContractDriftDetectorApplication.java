package com.contractdrift;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.contractdrift.application.CompareContractsUseCase;
import com.contractdrift.domain.Change;
import com.contractdrift.domain.Config;
import com.contractdrift.domain.Severity;
import com.contractdrift.infrastructure.config.ConfigLoader;
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
 *   contract-drift-detector &lt;old-contract.yaml&gt; &lt;new-contract.yaml&gt; [--json] [--config config.yaml]
 * </pre>
 */
public class ContractDriftDetectorApplication {

    public static void main(String[] args) {
        boolean jsonOutput = false;
        String oldContractPath = null;
        String newContractPath = null;
        String configPath = null;

        for (int i = 0; i < args.length; i++) {
            if ("--json".equals(args[i])) {
                jsonOutput = true;
            } else if ("--config".equals(args[i]) && i + 1 < args.length) {
                configPath = args[++i];
            } else if (oldContractPath == null) {
                oldContractPath = args[i];
            } else if (newContractPath == null) {
                newContractPath = args[i];
            }
        }

        if (oldContractPath == null || newContractPath == null) {
            System.err.println(
                    "Usage: contract-drift-detector <old-contract.yaml> <new-contract.yaml> [--json] [--config config.yaml]");
            System.exit(1);
        }

        Config config = loadConfig(configPath);

        Path oldContract = Path.of(oldContractPath);
        Path newContract = Path.of(newContractPath);

        CompareContractsUseCase useCase = new CompareContractsUseCase(new OpenApiParserAdapter());

        List<Change> changes = useCase.execute(oldContract, newContract, config);

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

    private static Config loadConfig(String configPath) {
        if (configPath == null) {
            return Config.defaultConfig();
        }

        try {
            ConfigLoader loader = new ConfigLoader();
            return loader.load(Path.of(configPath));
        } catch (IOException e) {
            System.err.println("Error loading config: " + e.getMessage());
            System.exit(1);
            return null;
        }
    }
}
