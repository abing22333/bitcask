package com.abing.raft.server;

import com.abing.raft.client.KvArgument;
import com.abing.raft.client.KvResult;
import com.abing.raft.client.ServerInfos;
import com.abing.raft.server.entity.AppendEntryArgument;
import com.abing.raft.server.entity.AppendEntryResult;
import com.abing.raft.server.entity.RequestVoteArgument;
import com.abing.raft.server.entity.RequestVoteResult;
import com.abing.raft.server.impl.DefalutlRaft;
import com.abing.raft.server.state.RuleStrategy;

import java.util.logging.Logger;

/**
 * Raft算法
 *
 * @author abing
 * @date 2023/10/10
 */

public interface Raft {
    Logger log = Logger.getLogger(Raft.class.getName());

    /**
     * 场景raft
     * @param arg
     * @return
     */

    static Raft createRaft(String arg) {
        ServerInfos serverInfos = ServerInfos.createServerInfos(arg);
        DefalutlRaft defalutlRaft = new DefalutlRaft(serverInfos);
        defalutlRaft.setRule(RuleStrategy.FOLLOWER);

        return defalutlRaft;
    }


    AppendEntryResult appendEntry(AppendEntryArgument appendEntry);

    RequestVoteResult requestVote(RequestVoteArgument requestVote);


    KvResult<?> clientKv(KvArgument<?> argument);

    void setRule(RuleStrategy rule);

    int getPort();

    String getNodeDesc();

    void start();
}
