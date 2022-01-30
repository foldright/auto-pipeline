package com.foldright.examples.duplex.handler;

public class Request {
    private final String uri;

    public Request(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }
}
