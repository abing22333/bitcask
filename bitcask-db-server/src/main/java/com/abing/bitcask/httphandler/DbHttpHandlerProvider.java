package com.abing.bitcask.httphandler;

import com.abing.bitcask.common.api.BitCask;
import com.abing.bitcask.common.constant.DbHttpPaths;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * 数据库HttpHandler provider
 *
 * @author abing
 * @date 2023/9/27
 */
public class DbHttpHandlerProvider {
    /**
     * 向server添加context
     *
     * @param server  server
     * @param bitCask bitCask
     */
    public static void provider(HttpServer server, BitCask bitCask) {

        server.createContext(DbHttpPaths.DB_GET, new AbstractHandler() {
            @Override
            protected byte[] doHandle(HttpExchange exchange) {
                return bitCask.get(getQueryParam("key"));
            }
        });

        server.createContext(DbHttpPaths.DB_SET, new AbstractHandler() {
            @Override
            protected byte[] doHandle(HttpExchange exchange) {
                bitCask.put(getQueryParam("key"), getRequestBody());
                return new byte[0];
            }
        });

        server.createContext(DbHttpPaths.DB_DELETE, new AbstractHandler() {
            @Override
            protected byte[] doHandle(HttpExchange exchange) {
                bitCask.delete(getQueryParam("key"));
                return new byte[0];
            }
        });
    }
}
