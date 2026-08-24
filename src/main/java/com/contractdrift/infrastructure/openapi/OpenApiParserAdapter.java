package com.contractdrift.infrastructure.openapi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.contractdrift.domain.Contract;
import com.contractdrift.domain.ContractParser;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

/**
 * Adapter that reads OpenAPI files and converts them to domain {@link Contract}.
 *
 * <p>
 * This is an <strong>input adapter</strong> in hexagonal architecture.
 * It depends on Swagger Parser for I/O and on {@link OpenApiMapper} for
 * structural translation. The mapper is a separate class to keep this
 * adapter focused on reading files.
 */
public class OpenApiParserAdapter implements ContractParser {

    private final OpenApiMapper mapper = new OpenApiMapper();

    @Override
    public Contract parse(Path filePath) {
        try {
            String content = Files.readString(filePath);
            return parse(content);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read file: " + filePath, e);
        }
    }

    public Contract parse(String content) {
        SwaggerParseResult result = parseOpenApi(content);
        OpenAPI openAPI = result.getOpenAPI();

        if (openAPI == null) {
            throw new IllegalArgumentException("Invalid OpenAPI content: " + result.getMessages());
        }

        return mapper.toContract(openAPI);
    }

    private SwaggerParseResult parseOpenApi(String content) {
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setResolveFully(true);
        return new OpenAPIParser().readContents(content, null, options);
    }
}
