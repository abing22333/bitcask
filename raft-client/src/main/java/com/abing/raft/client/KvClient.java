package com.abing.raft.client;

import java.io.IOException;
import java.net.http.HttpClient;

/**
 * @author abing
 * @date 2023/10/20
 */
public class KvClient extends BaseClient {

    public KvClient(HttpClient httpClient, ServerInfos serverInfos) {
        this.httpClient = httpClient;
        this.serverInfos = serverInfos;
    }

    ServerInfos serverInfos;

    public KvResult<?> clientKv(KvArgument<?> kvArgument) {
        KvResult<?> result;

        try {
            result = post(serverInfos.getSelf().getAddr(), "/kv", kvArgument);
        } catch (IOException | InterruptedException e) {
            result = KvResult.fail();
        }
        return result;
    }
}
