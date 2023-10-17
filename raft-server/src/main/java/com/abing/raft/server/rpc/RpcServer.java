package com.abing.raft.server.rpc;

import com.abing.raft.server.MachineInfos;
import com.abing.raft.server.entity.AppendEntryArgument;
import com.abing.raft.server.entity.AppendEntryResult;
import com.abing.raft.server.entity.RequestVoteArgument;
import com.abing.raft.server.entity.RequestVoteResult;

/**
 * @author abing
 * @date 2023/10/11
 */
public interface RpcServer {
    /**
     * 追加条目：由领导人调用，用于日志条目的复制，同时也被当做心跳使用
     *
     * @param appendEntry 参数
     * @param machineInfo 发送对象
     * @return AppendEntryResult
     */
    AppendEntryResult appendEntry(AppendEntryArgument appendEntry, MachineInfos.MachineInfo machineInfo);

    /**
     * 请求投票: 由候选人负责调用用来征集选票
     *
     * @param requestVote arg
     * @param machineInfo 发送对象
     * @return RequestVoteResult
     */
    RequestVoteResult requestVote(RequestVoteArgument requestVote, MachineInfos.MachineInfo machineInfo);

    RequestVoteResult sysnrequestVote(RequestVoteArgument requestVote, MachineInfos.MachineInfo machineInfo);
}
