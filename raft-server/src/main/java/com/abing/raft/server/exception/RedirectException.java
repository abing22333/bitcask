package com.abing.raft.server.exception;

/**
 * @author abing
 * @date 2023/10/18
 */
public class RedirectException extends RuntimeException {
    private String uri;

    public RedirectException(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }
}
