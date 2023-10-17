package com.abing.raft.server;

import com.abing.raft.server.httphandler.RaftHttpHandlerProvider;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * BitCask 服务器
 *
 * @author abing
 */
public class BitCaskServer {
    static Logger log = Logger.getLogger(BitCaskServer.class.getName());

    public static void main(String[] args) throws IOException {
        // 创建bitCask

        Raft raft = new Raft(args[0]);

        // 创建HTTP服务器，绑定到指定的主机和端口
        int port = raft.getPort();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        RaftHttpHandlerProvider.provider(server, raft);
        server.setExecutor(Executors.newFixedThreadPool(10));
        // 启动HTTP服务器
        server.start();
        log.info("Raft服务器已启动，监听端口: " + port);
    }
}
