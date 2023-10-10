package com.abing.raft.server.httphandler;

import com.abing.kv.common.exception.ServerException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;

public abstract class AbstractHandler implements HttpHandler {

    ThreadLocal<Map<String, List<String>>> queryParamsThreadLocal = new ThreadLocal<>();
    ThreadLocal<byte[]> reqestBodyThreadLocal = new ThreadLocal<>();

    protected String getQueryParam(String key) {

        return Optional.ofNullable(getQueryParams(key))
                .map(params -> params.get(0))
                .orElse(null);
    }

    protected List<String> getQueryParams(String key) {
        return Optional.ofNullable(queryParamsThreadLocal.get())
                .map(queryParams -> queryParams.get(key))
                .orElse(null);
    }

    protected byte[] getRequestBody() {
        return reqestBodyThreadLocal.get();
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        parseQueryParameters(exchange.getRequestURI().getQuery());
        parseRequestBody(exchange);

        byte[] bytes;

        try {
            bytes = doHandle(exchange);
            exchange.sendResponseHeaders(200, bytes.length);
        } catch (ServerException e) {
            bytes = e.getMessage().getBytes();
            exchange.sendResponseHeaders(400, bytes.length);
        } catch (Exception e) {
            bytes = "服务器异常".getBytes();
            exchange.sendResponseHeaders(500, bytes.length);
        }

        queryParamsThreadLocal.remove();
        reqestBodyThreadLocal.remove();

        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private void parseQueryParameters(String uri) {
        String[] params = uri.split("&");
        Map<String, List<String>> queryParams = new HashMap<>(params.length);

        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];
                queryParams.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            }
        }

        queryParamsThreadLocal.set(queryParams);
    }

    protected void parseRequestBody(HttpExchange exchange) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
        ReadableByteChannel readableByteChannel = Channels.newChannel(exchange.getRequestBody());
        int read = readableByteChannel.read(buffer);
        if (read < 0) {
            return;
        }

        buffer.flip();
        byte[] bytes = new byte[read];
        buffer.get(bytes);

        reqestBodyThreadLocal.set(bytes);
    }


    /**
     * 执行操作
     *
     * @param exchange 执行操作
     * @return 结果byte
     */
    protected abstract byte[] doHandle(HttpExchange exchange);
}