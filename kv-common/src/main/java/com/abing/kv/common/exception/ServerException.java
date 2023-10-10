package com.abing.kv.common.exception;

/**
 * @author abing
 * @date 2023/9/26
 */
public class ServerException extends RuntimeException {
    public ServerException(String message) {
        super(message);
    }
}
