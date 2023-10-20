package com.abing.raft.client;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.rmi.ServerException;
import java.text.MessageFormat;
import java.util.logging.Logger;

public abstract class AbstractHandler implements HttpHandler {

    static Logger log = Logger.getLogger(AbstractHandler.class.getName());
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            log.info(MessageFormat.format("from: [{0}], uri:[{1}]", exchange.getRemoteAddress(), exchange.getRequestURI()));

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

            exchange.getResponseBody().write(bytes);
            exchange.close();
        }


        /**
         * 执行操作
         *
         * @param exchange 执行操作
         * @return 结果byte
         */
        protected abstract byte[] doHandle(HttpExchange exchange);
    }