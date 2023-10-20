package com.abing.raft.server.rpc;

import com.abing.raft.client.BaseClient;
import com.abing.raft.client.ServerInfos;
import com.abing.raft.server.constant.ApiPath;
import com.abing.raft.server.entity.AppendEntryArgument;
import com.abing.raft.server.entity.AppendEntryResult;
import com.abing.raft.server.entity.RequestVoteArgument;
import com.abing.raft.server.entity.RequestVoteResult;
import com.abing.raft.server.impl.DefalutlRaft;

import java.io.IOException;
import java.net.http.HttpClient;
import java.text.MessageFormat;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * @author abing
 * @date 2023/10/11
 */
public class HttpRpcServer extends BaseClient implements RpcServer {

    static Logger log = Logger.getLogger(HttpRpcServer.class.getName());
    private final DefalutlRaft defalutlRaft;

    public HttpRpcServer(DefalutlRaft defalutlRaft) {
        this.defalutlRaft = defalutlRaft;
        this.httpClient = HttpClient.newBuilder()
                .executor(Executors.newFixedThreadPool(defalutlRaft.getServerInfos().nodeSize()))
                .build();
    }


    @Override
    public AppendEntryResult appendEntry(AppendEntryArgument appendEntry, ServerInfos.ServerInfo serverInfo) {
        AppendEntryResult result;

        log.info(MessageFormat.format("{0}  send appendEntry: [{1}]", defalutlRaft.getNodeDesc(), appendEntry.toString()));
        try {
            result = post(serverInfo.getAddr(), ApiPath.APPEND_ENTRY, appendEntry);
        } catch (IOException e) {
            result = AppendEntryResult.fail(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.info(MessageFormat.format("{0}  receive appendEntry: [{1}]", defalutlRaft.getNodeDesc(), result.toString()));

        return result;
    }

    @Override
    public RequestVoteResult requestVote(RequestVoteArgument requestVote, ServerInfos.ServerInfo serverInfo) {
        RequestVoteResult voteResult;

        log.info(MessageFormat.format("{0} send requestVote to [{1}]  [{2}]", defalutlRaft.getNodeDesc(), serverInfo, requestVote));
        try {
            voteResult = post(serverInfo.getAddr(), ApiPath.REQUEST_VOTE, requestVote);
        } catch (IOException e) {
            voteResult = RequestVoteResult.disapprove(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info(MessageFormat.format("{0} receive requestVote from [{1}]  [{2}]", defalutlRaft.getNodeDesc(), serverInfo, voteResult));

        return voteResult;
    }

    @Override
    public RequestVoteResult sysnrequestVote(RequestVoteArgument requestVote, ServerInfos.ServerInfo serverInfo) {
        return null;
    }


}
