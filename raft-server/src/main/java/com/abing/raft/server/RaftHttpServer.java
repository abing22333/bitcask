package com.abing.raft.server;

import com.abing.raft.client.AbstractHandler;
import com.abing.raft.client.KvArgument;
import com.abing.raft.client.KvResult;
import com.abing.raft.client.SerializationUtil;
import com.abing.raft.server.constant.ApiPath;
import com.abing.raft.server.entity.AppendEntryArgument;
import com.abing.raft.server.entity.AppendEntryResult;
import com.abing.raft.server.entity.RequestVoteArgument;
import com.abing.raft.server.entity.RequestVoteResult;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * raft 服务器
 *
 * @author abing
 */
public class RaftHttpServer {
    static Logger log = Logger.getLogger(RaftHttpServer.class.getName());

    public static void main(String[] args) throws IOException {
        // 创建raft
        Raft raft = Raft.createRaft(args[0]);

        // 创建HTTP服务器，绑定到指定的主机和端口
        int port = raft.getPort();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        RaftHttpHandlerProvider.provider(server, raft);
        server.setExecutor(Executors.newFixedThreadPool(10));

        // 启动HTTP服务器
        server.start();
        // 启动raft
        raft.start();

        log.info("Raft服务器已启动，监听端口: " + port);
    }


    static class RaftHttpHandlerProvider {

        /**
         * 向server添加context
         *
         * @param server server
         * @param raft   Raft
         */
        public static void provider(HttpServer server, Raft raft) {

            server.createContext(ApiPath.REQUEST_VOTE, new AbstractHandler() {
                @Override
                protected byte[] doHandle(HttpExchange exchange) {
                    RequestVoteArgument argument = SerializationUtil.deserialize(exchange.getRequestBody());
                    RequestVoteResult result;
                    try {
                        log.info(MessageFormat.format("{0}  receive requestVote: [{1}]", raft.getNodeDesc(), argument.toString()));
                        result = raft.requestVote(argument);
                        log.info(MessageFormat.format("{0}  response requestVote: [{1}]", raft.getNodeDesc(), result.toString()));
                        return SerializationUtil.serialize(result);
                    } catch (Exception e) {
                        log.warning(e.getMessage());
                        result = RequestVoteResult.disapprove(0);
                    }
                    return SerializationUtil.serialize(result);
                }
            });

            server.createContext(ApiPath.APPEND_ENTRY, new AbstractHandler() {
                @Override
                protected byte[] doHandle(HttpExchange exchange) {
                    AppendEntryArgument argument = SerializationUtil.deserialize(exchange.getRequestBody());
                    AppendEntryResult result;
                    try {
                        log.info(MessageFormat.format("{0}  receive appendEntry: [{1}]", raft.getNodeDesc(), argument.toString()));
                        result = raft.appendEntry(argument);
                        log.info(MessageFormat.format("{0}  response appendEntry: [{1}]", raft.getNodeDesc(), result.toString()));
                    } catch (Exception e) {
                        log.warning(e.getMessage());
                        result = AppendEntryResult.fail(0);
                    }
                    return SerializationUtil.serialize(result);
                }
            });

            server.createContext("/api/kv", new AbstractHandler() {
                @Override
                protected byte[] doHandle(HttpExchange exchange) {
                    KvArgument<?> argument = SerializationUtil.deserialize(exchange.getRequestBody());
                    KvResult<?> result;
                    try {
                        log.info(MessageFormat.format("{0}  receive kv: [{1}]", raft.getNodeDesc(), argument.toString()));
                        result = raft.clientKv(argument);
                    } catch (Exception e) {
                        log.warning(e.getMessage());
                        result = KvResult.fail();
                    }
                    return SerializationUtil.serialize(result);
                }
            });
        }

    }
}
