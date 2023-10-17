package com.abing.raft.server.httphandler;

import com.abing.kv.common.exception.ServerException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

public abstract class AbstractHandler implements HttpHandler {
    static Logger log = Logger.getLogger(AbstractHandler.class.getName());

    ThreadLocal<Map<String, List<String>>> queryParamsThreadLocal = new ThreadLocal<>();

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


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.info(MessageFormat.format("from: [{0}], uri:[{1}]", exchange.getRemoteAddress(), exchange.getRequestURI()));
        parseQueryParameters(exchange.getRequestURI().getQuery());

        byte[] bytes;

        try {
            bytes = doHandle(exchange);
            exchange.sendResponseHeaders(200, bytes.length);
        } catch (ServerException e) {
            bytes = e.getMessage().getBytes();
            exchange.sendResponseHeaders(400, bytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            bytes = "服务器异常".getBytes();
            exchange.sendResponseHeaders(500, bytes.length);
        }

        queryParamsThreadLocal.remove();
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private void parseQueryParameters(String uri) {
        if (uri == null) {
            return;
        }
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

    protected byte[] getRequestBody(HttpExchange exchange) {
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
        ReadableByteChannel readableByteChannel = Channels.newChannel(exchange.getRequestBody());
        int read = 0;
        try {
            read = readableByteChannel.read(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (read < 0) {
            return new byte[]{};
        }

        buffer.flip();
        byte[] bytes = new byte[read];
        buffer.get(bytes);
        return bytes;
    }


    /**
     * 执行操作
     *
     * @param exchange 执行操作
     * @return 结果byte
     */
    protected abstract byte[] doHandle(HttpExchange exchange);
}