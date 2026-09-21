package com.contractdrift.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.contractdrift.domain.Config;

class ConfigLoaderTest {

    private final ConfigLoader loader = new ConfigLoader();

    @Test
    @DisplayName("should parse empty config")
    void shouldParseEmptyConfig() {
        Config config = loader.parse("");

        assertThat(config.ignoredRules()).isEmpty();
        assertThat(config.failOnWarning()).isFalse();
    }

    @Test
    @DisplayName("should parse ignored rules")
    void shouldParseIgnoredRules() {
        String yaml = """
                ignoredRules:
                  - PARAMETER_REMOVED
                  - RESPONSE_PROPERTY_REMOVED
                """;

        Config config = loader.parse(yaml);

        assertThat(config.ignoredRules())
                .containsExactlyInAnyOrder("PARAMETER_REMOVED", "RESPONSE_PROPERTY_REMOVED");
    }

    @Test
    @DisplayName("should parse failOnWarning")
    void shouldParseFailOnWarning() {
        String yaml = """
                failOnWarning: true
                """;

        Config config = loader.parse(yaml);

        assertThat(config.failOnWarning()).isTrue();
    }

    @Test
    @DisplayName("should parse full config")
    void shouldParseFullConfig() {
        String yaml = """
                ignoredRules:
                  - PARAMETER_REMOVED
                  - ENDPOINT_REMOVED
                failOnWarning: true
                """;

        Config config = loader.parse(yaml);

        assertThat(config.ignoredRules())
                .containsExactlyInAnyOrder("PARAMETER_REMOVED", "ENDPOINT_REMOVED");
        assertThat(config.failOnWarning()).isTrue();
    }

    @Test
    @DisplayName("should check if rule is enabled")
    void shouldCheckIfRuleEnabled() {
        Config config = new Config(Set.of("PARAMETER_REMOVED"), false);

        assertThat(config.isRuleEnabled("PARAMETER_REMOVED")).isFalse();
        assertThat(config.isRuleEnabled("ENDPOINT_REMOVED")).isTrue();
    }
}
