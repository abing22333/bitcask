package com.abing.raft.server.state;


import com.abing.raft.client.ServerInfos;
import com.abing.raft.server.entity.LogEntry;
import com.abing.raft.server.entity.RequestVoteArgument;
import com.abing.raft.server.entity.RequestVoteResult;
import com.abing.raft.server.impl.DefalutlRaft;

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
public class CandidateStrategy extends RuleStrategy {

    static Logger log = Logger.getLogger(FollowerStrategy.class.getName());

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
                log.info(MessageFormat.format("{0}[Candidate] 开始选举", defalutlRaft.getId()));
                defalutlRaft.incrCurrentTerm();
                defalutlRaft.votedForSelf();
                sendSet.clear();
            }

            // 发送请求投票的 RPC 给其他所有服务器
            sendRequestVote();
        }
    }

    @Override
    public void bind(DefalutlRaft defalutlRaft) {
        super.bind(defalutlRaft);
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

        ServerInfos.ServerInfo self = defalutlRaft.getServerInfos().getSelf();
        Collection<ServerInfos.ServerInfo> otherNodeList = defalutlRaft.getServerInfos().getOtherNodeInfo();

        long lastLogTerm = 0;
        long lastLogIndex = 0;
        LogEntry lastLogEntry = defalutlRaft.getLast();

        if (lastLogEntry != null) {
            lastLogTerm = lastLogEntry.getTerm();
            lastLogIndex = lastLogEntry.getIndex();
        }

        RequestVoteArgument requestVote = RequestVoteArgument.builder()
                .withCandidateId(self.getId())
                .withTerm(defalutlRaft.getCurrentTerm())
                .withLastLogIndex(lastLogIndex)
                .withLastLogTerm(lastLogTerm)
                .build();

        int count = 1, total = otherNodeList.size() + 1;

        for (ServerInfos.ServerInfo serverInfo : otherNodeList) {
            if (isStop) {
                return;
            }

            if (sendSet.contains(serverInfo.getId())) {
                return;
            }
            try {

                RequestVoteResult voteResult = defalutlRaft.getRpcServer().requestVote(requestVote, serverInfo);

                // 接收到的 RPC 请求或响应，需要检查任期
                defalutlRaft.checkTerm(voteResult.getTerm());

                if (voteResult.getVoteGranted()) {
                    count++;
                }

                log.info(MessageFormat.format("{0} count: {1}", defalutlRaft.getNodeDesc(), count));
                // 获得大多数投票
                if (count >= total / 2 + 1) {
                    // 切换到领导者状态
                    tryChangeRule(RuleStrategy.LEADER);
                }

                sendSet.add(serverInfo.getId());
            } catch (Exception e) {
                log.info(e.getMessage());
            }

        }
    }
}
