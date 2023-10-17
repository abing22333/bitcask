package com.abing.raft.server.state;

import com.abing.raft.server.LogModule;
import com.abing.raft.server.MachineInfos;
import com.abing.raft.server.StateMachine;
import com.abing.raft.server.entity.*;
import com.abing.raft.server.impl.DefaultLogModule;
import com.abing.raft.server.impl.DefaultStateMachine;
import com.abing.raft.server.rpc.HttpRpcServer;
import com.abing.raft.server.rpc.RpcServer;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * 通用节点角色
 *
 * @author abing
 * @date 2023/10/11
 */
public class RaftNode {


    static Logger log = Logger.getLogger(RaftNode.class.getName());

    /* ============  所有服务器上的易失性状态=============  */

    /**
     * 已知已提交的最高的日志条目的索引（初始值为0，单调递增）
     */
    long commitIndex = 0;

    /**
     * 已经被应用到状态机的最高的日志条目的索引（初始值为0，单调递增）
     */
    long lastApplied = 0;


    /* ============ 所有服务器上的持久性状态 =============  */

    /**
     * 服务器已知最新的任期（在服务器首次启动时初始化为0，单调递增）
     */
    long currentTerm = 0;

    /**
     * 当前任期内收到选票的 candidateId，如果没有投给任何候选人 则为空
     */
    String votedFor;

    /**
     * 日志条目集合；每个条目包含了用于状态机的命令，以及领导人接收到该条目时的任期（初始索引为1）
     */
    LogModule logModule = new DefaultLogModule();

    StateMachine stateMachine = new DefaultStateMachine();

    RpcServer rpcServer = new HttpRpcServer();

    MachineInfos machineInfos;

    Rule rule;

      long lastHearBeat;

    public RaftNode(MachineInfos machineInfos) {
        this.machineInfos = machineInfos;
    }

    public RaftNode(RaftNode raftNode) {
        this(raftNode.machineInfos);
        this.commitIndex = raftNode.commitIndex;
        this.currentTerm = raftNode.currentTerm;
        this.lastApplied = raftNode.lastApplied;
        this.votedFor = raftNode.votedFor;
        this.logModule = raftNode.logModule;
        this.stateMachine = raftNode.stateMachine;
        this.rpcServer = raftNode.rpcServer;
    }

    public Rule getRule() {
        return rule;
    }

    public RaftNode setRule(Rule rule) {
        this.rule = rule;
        this.rule.bind(this);
        return this;
    }

    public void doSameThing() {
        rule.doSameThing();
    }

    /**
     * 将日志应用到状态机中
     */

    public void apply() {
        while (commitIndex > lastApplied) {
            stateMachine.apply(logModule.getLogEntry(++lastApplied));
        }
    }

    /**
     * 检查任期：接收到请求或响应的通用操作
     *
     * @param term 请求或响应中的term
     */
    public synchronized void checkTerm(long term) {
        lastHearBeat = System.currentTimeMillis();

        if (term > currentTerm) {

            currentTerm = term;

            // 尝试切换到跟随者角色
            rule.tryChangeRule(Rule.FOLLOWER);
        }
    }


    /**
     * 追加条目：由领导人调用，用于日志条目的复制，同时也被当做心跳使用
     *
     * @param appendEntry 参数
     * @return AppendEntryResult
     */
    public AppendEntryResult appendEntry(AppendEntryArgument appendEntry) {
        checkTerm(appendEntry.getTerm());

        if (appendEntry.getTerm() < currentTerm) {
            return AppendEntryResult.fail(currentTerm);
        }

        LogEntry logEntry = logModule.getLogEntry(appendEntry.getPrevLogIndex());
        if (logEntry == null) {
            return AppendEntryResult.fail(currentTerm);
        }

        // 日志冲突
        if (!Objects.equals(logEntry.getTerm(), appendEntry.getPreLogTerm())) {
            logModule.removeOnStartIndex(logEntry.getIndex());
        }

        List<LogEntry> entries = appendEntry.getEntries();
        if (CollectionUtils.isNotEmpty(entries)) {
            logModule.write(entries);
        }

        // 同步
        long leaderCommit = appendEntry.getLeaderCommit();
        if (leaderCommit > commitIndex) {
            // 提交
            long lastIndex = logModule.lastIndex();
            commitIndex = Math.min(leaderCommit, lastIndex);
        }

        apply();

        return AppendEntryResult.success(currentTerm);
    }

    /**
     * 请求投票: 由候选人负责调用用来征集选票
     *
     * @param requestVote arg
     * @return RequestVoteResult
     */
    public RequestVoteResult requestVote(RequestVoteArgument requestVote) {


        long voteTerm = requestVote.getTerm();
        checkTerm(voteTerm);

        // 对方任期没有自己新
        if (voteTerm < currentTerm) {
            return RequestVoteResult.disapprove(currentTerm);
        }

        // 没有投票，或者给后候选人投过票
        String candidateId = requestVote.getCandidateId();
        if (Objects.isNull(votedFor) || Objects.equals(votedFor, candidateId)) {
            // 候选人的日志至少和自己一样新, 才投票给它
            LogEntry lastLog = logModule.getLast();
            if (lastLog == null || lastLog.lessOrEqual(requestVote.getLastLogTerm(), requestVote.getLastLogIndex())) {
                votedFor = candidateId;

                return RequestVoteResult.approve(currentTerm);
            }
        }

        return RequestVoteResult.disapprove(currentTerm);
    }

    public Rule nextRule;

    public String getId() {
        return machineInfos.getSelf().getId();
    }

    public String getNodeInfo() {
        return getId() + "[" + rule.display() + "]";
    }
}
