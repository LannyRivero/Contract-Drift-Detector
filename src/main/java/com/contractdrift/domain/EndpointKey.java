package com.contractdrift.domain;

public record EndpointKey(String path, HttpMethod method) {

    public String displayName() {
        return method + " " + path;
    }
}
