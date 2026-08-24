package com.contractdrift.infrastructure.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import com.contractdrift.domain.Config;

/**
 * Loads configuration from a YAML file.
 *
 * <p>
 * Config file format:
 * 
 * <pre>
 * ignoredRules:
 *   - PARAMETER_REMOVED
 *   - RESPONSE_PROPERTY_REMOVED
 * failOnWarning: false
 * </pre>
 */
public class ConfigLoader {

    /**
     * Loads config from a YAML file.
     *
     * @param path path to config file
     * @return parsed config
     * @throws IOException if file cannot be read
     */
    public Config load(Path path) throws IOException {
        String content = Files.readString(path);
        return parse(content);
    }

    /**
     * Parses config from YAML content string.
     *
     * @param yaml YAML content
     * @return parsed config
     */
    Config parse(String yaml) {
        Set<String> ignoredRules = new HashSet<>();
        boolean failOnWarning = false;

        String[] lines = yaml.split("\n");
        boolean inIgnoredRules = false;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith("ignoredRules:")) {
                inIgnoredRules = true;
                continue;
            }

            if (inIgnoredRules && trimmed.startsWith("- ")) {
                String rule = trimmed.substring(2).trim();
                if (!rule.isEmpty()) {
                    ignoredRules.add(rule);
                }
                continue;
            }

            if (trimmed.startsWith("failOnWarning:")) {
                failOnWarning = Boolean.parseBoolean(trimmed.substring(14).trim());
                inIgnoredRules = false;
            }

            if (!trimmed.startsWith("-") && !trimmed.isEmpty() && inIgnoredRules) {
                inIgnoredRules = false;
            }
        }

        return new Config(ignoredRules, failOnWarning);
    }
}
