package com.abing.raft.server.httphandler;

import com.abing.kv.common.util.SerializationUtil;
import com.abing.raft.server.Raft;
import com.abing.raft.server.entity.AppendEntryArgument;
import com.abing.raft.server.entity.AppendEntryResult;
import com.abing.raft.server.entity.RequestVoteArgument;
import com.abing.raft.server.entity.RequestVoteResult;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * raft暴露接口 HttpHandler provider
 *
 * @author abing
 * @date 2023/9/27
 */
public class RaftHttpHandlerProvider {
    /**
     * 向server添加context
     *
     * @param server server
     * @param raft   Raft
     */
    public static void provider(HttpServer server, Raft raft) {

        server.createContext("/requestVote", new AbstractHandler() {
            @Override
            protected byte[] doHandle(HttpExchange exchange) {
                RequestVoteArgument argument = SerializationUtil.deserialize(exchange.getRequestBody());
                RequestVoteResult result;
                try {
                    result = raft.requestVote(argument);
                    return SerializationUtil.serialize(result);
                } catch (Exception e) {
                    log.warning(e.getMessage());
                    result = RequestVoteResult.disapprove(0);
                }
                return SerializationUtil.serialize(result);
            }
        });

        server.createContext("/appendEntry", new AbstractHandler() {
            @Override
            protected byte[] doHandle(HttpExchange exchange) {
                AppendEntryArgument argument = SerializationUtil.deserialize(exchange.getRequestBody());
                AppendEntryResult result;
                try {
                    result = raft.appendEntry(argument);
                } catch (Exception e) {
                    log.warning(e.getMessage());
                    result = AppendEntryResult.fail(0);
                }
                return SerializationUtil.serialize(result);
            }
        });
    }
}
