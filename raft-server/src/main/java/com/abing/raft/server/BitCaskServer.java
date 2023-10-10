package com.abing.raft.server;

import com.abing.kv.bitcask.BitCaskFactory;
import com.abing.kv.common.api.KvDataBase;
import com.abing.raft.server.httphandler.DbHttpHandlerProvider;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * BitCask 服务器
 *
 * @author abing
 */
public class BitCaskServer {


    public static void main(String[] args) throws IOException {
        // 创建bitCask
        KvDataBase bitCask = BitCaskFactory.create();

        // 创建HTTP服务器，绑定到指定的主机和端口
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // 设置上下文
        DbHttpHandlerProvider.provider(server, bitCask);

        // 启动HTTP服务器
        server.start();

        System.out.println("BitCask服务器已启动，监听端口 " + port);
    }
}
