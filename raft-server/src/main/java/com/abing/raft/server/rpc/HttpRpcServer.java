package com.abing.raft.server.rpc;

import com.abing.kv.common.util.SerializationUtil;
import com.abing.raft.server.MachineInfos;
import com.abing.raft.server.entity.AppendEntryArgument;
import com.abing.raft.server.entity.AppendEntryResult;
import com.abing.raft.server.entity.RequestVoteArgument;
import com.abing.raft.server.entity.RequestVoteResult;
import com.abing.raft.server.state.RaftNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * @author abing
 * @date 2023/10/11
 */
public class HttpRpcServer implements RpcServer {

    static Logger log = Logger.getLogger(RaftNode.class.getName());

    HttpClient httpClient;

    {
        httpClient = HttpClient.newBuilder()
                .executor(Executors.newFixedThreadPool(10))
                .build();
    }

    @Override
    public AppendEntryResult appendEntry(AppendEntryArgument appendEntry, MachineInfos.MachineInfo machineInfo) {
        byte[] serialize = SerializationUtil.serialize(appendEntry);
        try {
            HttpResponse<byte[]> response = post(machineInfo.getAddr(), "/appendEntry", serialize);

            return SerializationUtil.deserialize(response.body());
        } catch (IOException | InterruptedException e) {

        }
        return AppendEntryResult.fail(0);
    }

    @Override
    public RequestVoteResult requestVote(RequestVoteArgument requestVote, MachineInfos.MachineInfo machineInfo) {

        RequestVoteResult voteResult;

        try {
            byte[] serialize = SerializationUtil.serialize(requestVote);
            HttpResponse<byte[]> response = post(machineInfo.getAddr(), "/requestVote", serialize);
            voteResult = SerializationUtil.deserialize(response.body());
        } catch (IOException e) {
            voteResult = RequestVoteResult.disapprove(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return voteResult;
    }

    @Override
    public RequestVoteResult sysnrequestVote(RequestVoteArgument requestVote, MachineInfos.MachineInfo machineInfo) {
        return null;
    }

    private HttpResponse<byte[]> post(String host, String path, byte[] value) throws IOException, InterruptedException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://%s%s", host, path)))
                .POST(HttpRequest.BodyPublishers.ofByteArray(value))
                .build();

        HttpResponse<byte[]> response;

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());


        return response;
    }


}
