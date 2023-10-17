package com.abing.raft.server.state;


import com.abing.raft.server.MachineInfos;
import com.abing.raft.server.entity.LogEntry;
import com.abing.raft.server.entity.RequestVoteArgument;
import com.abing.raft.server.entity.RequestVoteResult;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * 候选人角色
 *
 * @author abing
 * @date 2023/10/10
 */
public class CandidateRaft extends NodeRule {

    static Logger log = Logger.getLogger(FollowerRaft.class.getName());

    /**
     * 上次时间戳
     */
    long lastElectTime;

    /**
     * 超时间隔基数
     */
    private static final long TIME_OUT = 15 * 1000;

    private Set<String> sendSet = new HashSet<>();

    /**
     * 选举过程：<br/>
     * 1. 自增当前的任期号（currentTerm）<br/>
     * 2. 给自己投票<br/>
     * 3. 重置选举超时计时器<br/>
     * 4. 发送请求投票的 RPC 给其他所有服务器<br/>
     */
    @Override
    public void doSameThing() {

        while (true) {
            if (isStop) {
                return;
            }

            if (isTimeOut()) {
                log.info(MessageFormat.format("{0}[Candidate] 开始选举", raftNode.getId()));
                raftNode.currentTerm++;
                raftNode.votedFor = raftNode.machineInfos.getSelf().getId();
                sendSet.clear();
            }

            // 发送请求投票的 RPC 给其他所有服务器
            sendRequestVote();
        }
    }

    @Override
    public void bind(RaftNode raftNode) {
        super.bind(raftNode);
        this.lastElectTime = -1;
    }

    @Override
    public String display() {
        return "Candidate";
    }

    private boolean isTimeOut() {
        if (lastElectTime == -1 || System.currentTimeMillis() - lastElectTime > TIME_OUT) {
            lastElectTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * 发送请求投票给其他所有服务器
     */
    public void sendRequestVote() {

        MachineInfos.MachineInfo self = raftNode.machineInfos.getSelf();
        Collection<MachineInfos.MachineInfo> otherNodeList = raftNode.machineInfos.getOtherNodeInfo();

        long lastLogTerm = 0;
        long lastLogIndex = 0;
        LogEntry lastLogEntry = raftNode.logModule.getLast();

        if (lastLogEntry != null) {
            lastLogTerm = lastLogEntry.getTerm();
            lastLogIndex = lastLogEntry.getIndex();
        }

        RequestVoteArgument requestVote = RequestVoteArgument.builder()
                .withCandidateId(self.getId())
                .withTerm(raftNode.currentTerm)
                .withLastLogIndex(lastLogIndex)
                .withLastLogTerm(lastLogTerm)
                .build();

        int count = 1, total = otherNodeList.size() + 1;

        for (MachineInfos.MachineInfo machineInfo : otherNodeList) {
            if (isStop) {
                return;
            }

            if (sendSet.contains(machineInfo.getId())) {
                return;
            }
            try {
                log.info(MessageFormat.format("{0} send requestVote to [{1}]  [{2}]", raftNode.getNodeInfo(), machineInfo, requestVote));
                RequestVoteResult voteResult = raftNode.rpcServer.requestVote(requestVote, machineInfo);
                log.info(MessageFormat.format("{0} receive requestVote from [{1}]  [{2}]", raftNode.getNodeInfo(), machineInfo, voteResult));

                // 接收到的 RPC 请求或响应，需要检查任期
                raftNode.checkTerm(voteResult.getTerm());

                if (voteResult.getVoteGranted()) {
                    count++;
                }

                log.info(MessageFormat.format("{0} count: {1}", raftNode.getNodeInfo(), count));
                // 获得大多数投票
                if (count >= total / 2 + 1) {
                    // 切换到领导者状态
                    tryChangeRule(Rule.LEADER);
                }

                sendSet.add(machineInfo.getId());
            } catch (Exception e) {
                log.info(e.getMessage());
            }

        }
    }
}
