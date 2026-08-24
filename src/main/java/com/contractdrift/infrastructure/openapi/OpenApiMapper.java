package com.contractdrift.infrastructure.openapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.contractdrift.domain.ApiParameter;
import com.contractdrift.domain.ApiProperty;
import com.contractdrift.domain.ApiSchema;
import com.contractdrift.domain.Contract;
import com.contractdrift.domain.Endpoint;
import com.contractdrift.domain.EndpointKey;
import com.contractdrift.domain.HttpMethod;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;

/**
 * Converts Swagger's {@link OpenAPI} model into the domain {@link Contract}.
 *
 * <p>
 * This mapper exists to keep {@link OpenApiParserAdapter} focused on I/O
 * (reading files) while this class handles the structural translation.
 * Both classes live in the infrastructure layer.
 */
class OpenApiMapper {

    Contract toContract(OpenAPI openAPI) {
        Map<EndpointKey, Endpoint> endpoints = new HashMap<>();

        if (openAPI.getPaths() == null) {
            return new Contract(endpoints);
        }

        for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
            String path = pathEntry.getKey();
            PathItem pathItem = pathEntry.getValue();

            mapOperation(path, pathItem, PathItem.HttpMethod.GET, pathItem.getGet(), endpoints);
            mapOperation(path, pathItem, PathItem.HttpMethod.POST, pathItem.getPost(), endpoints);
            mapOperation(path, pathItem, PathItem.HttpMethod.PUT, pathItem.getPut(), endpoints);
            mapOperation(path, pathItem, PathItem.HttpMethod.PATCH, pathItem.getPatch(), endpoints);
            mapOperation(path, pathItem, PathItem.HttpMethod.DELETE, pathItem.getDelete(), endpoints);
            mapOperation(path, pathItem, PathItem.HttpMethod.HEAD, pathItem.getHead(), endpoints);
            mapOperation(path, pathItem, PathItem.HttpMethod.OPTIONS, pathItem.getOptions(), endpoints);
            mapOperation(path, pathItem, PathItem.HttpMethod.TRACE, pathItem.getTrace(), endpoints);
        }

        return new Contract(endpoints);
    }

    private void mapOperation(
            String path,
            PathItem pathItem,
            PathItem.HttpMethod httpMethod,
            Operation operation,
            Map<EndpointKey, Endpoint> endpoints
    ) {
        if (operation == null) {
            return;
        }

        HttpMethod method = HttpMethod.valueOf(httpMethod.name());
        EndpointKey key = new EndpointKey(path, method);

        List<ApiParameter> parameters = mapParameters(pathItem, operation);
        ApiSchema requestBody = mapRequestBody(operation);
        Map<String, ApiSchema> responses = mapResponses(operation);

        endpoints.put(key, new Endpoint(key, parameters, requestBody, responses));
    }

    private List<ApiParameter> mapParameters(PathItem pathItem, Operation operation) {
        List<ApiParameter> result = new ArrayList<>();

        if (pathItem.getParameters() != null) {
            for (Parameter param : pathItem.getParameters()) {
                result.add(mapParameter(param));
            }
        }

        if (operation.getParameters() != null) {
            for (Parameter param : operation.getParameters()) {
                result.add(mapParameter(param));
            }
        }

        return result;
    }

    private ApiParameter mapParameter(Parameter param) {
        String location = param.getIn() != null ? param.getIn() : "query";
        String type = "string";

        if (param.getSchema() != null && param.getSchema().getType() != null) {
            type = param.getSchema().getType();
        }

        return new ApiParameter(
                param.getName(),
                location,
                Boolean.TRUE.equals(param.getRequired()),
                type
        );
    }

    private ApiSchema mapRequestBody(Operation operation) {
        if (operation.getRequestBody() == null || operation.getRequestBody().getContent() == null) {
            return null;
        }

        io.swagger.v3.oas.models.media.MediaType mediaType =
                operation.getRequestBody().getContent().get("application/json");

        if (mediaType == null || mediaType.getSchema() == null) {
            return null;
        }

        return mapSchema(mediaType.getSchema());
    }

    ApiSchema mapSchema(Schema<?> schema) {
        if (schema == null) {
            return null;
        }

        String name = schema.getName();
        String type = schema.getType() != null ? schema.getType() : "object";

        Map<String, ApiProperty> properties = mapProperties(schema);
        Set<String> requiredProperties = mapRequired(schema);

        return new ApiSchema(name, type, properties, requiredProperties);
    }

    @SuppressWarnings("unchecked")
    private Map<String, ApiProperty> mapProperties(Schema<?> schema) {
        Map<String, ApiProperty> properties = new HashMap<>();

        Map schemaProps = schema.getProperties();
        if (schemaProps == null) {
            return properties;
        }

        List<String> requiredList = schema.getRequired();
        Set<String> required = requiredList != null ? new HashSet<>(requiredList) : Set.of();

        for (Object obj : schemaProps.entrySet()) {
            Map.Entry<String, Schema> entry = (Map.Entry<String, Schema>) obj;
            Schema propSchema = entry.getValue();
            String propName = entry.getKey();
            String propType = propSchema.getType() != null ? propSchema.getType() : "unknown";
            boolean propRequired = required.contains(propName);

            properties.put(propName, new ApiProperty(propName, propType, propRequired));
        }

        return properties;
    }

    private Set<String> mapRequired(Schema<?> schema) {
        Set<String> required = new HashSet<>();

        if (schema.getRequired() != null) {
            required.addAll(schema.getRequired());
        }

        return required;
    }

    private Map<String, ApiSchema> mapResponses(Operation operation) {
        Map<String, ApiSchema> responses = new HashMap<>();

        if (operation.getResponses() == null) {
            return responses;
        }

        for (Map.Entry<String, ApiResponse> entry : operation.getResponses().entrySet()) {
            String statusCode = entry.getKey();
            ApiResponse response = entry.getValue();

            if (response.getContent() != null) {
                io.swagger.v3.oas.models.media.MediaType mediaType =
                        response.getContent().get("application/json");

                if (mediaType != null && mediaType.getSchema() != null) {
                    responses.put(statusCode, mapSchema(mediaType.getSchema()));
                }
            }
        }

        return responses;
    }
}
