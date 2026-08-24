package com.contractdrift.domain;

import java.nio.file.Path;

/**
 * Port (interface) for parsing API contract files into domain objects.
 *
 * <p>
 * Implementations live in the infrastructure layer (e.g.
 * {@code OpenApiParserAdapter}). The application layer depends on this
 * interface, not on the concrete parser — following hexagonal architecture.
 */
public interface ContractParser {

    /**
     * Parses a contract file (YAML or JSON) into a {@link Contract}.
     *
     * @param filePath path to the OpenAPI specification file
     * @return the parsed contract
     * @throws IllegalArgumentException if the file cannot be read or parsed
     */
    Contract parse(Path filePath);
}
