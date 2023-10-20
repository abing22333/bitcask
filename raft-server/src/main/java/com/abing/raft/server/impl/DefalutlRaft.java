package com.abing.raft.server.impl;

import com.abing.raft.client.KvArgument;
import com.abing.raft.client.KvResult;
import com.abing.raft.client.ServerInfos;
import com.abing.raft.server.LogModule;
import com.abing.raft.server.Raft;
import com.abing.raft.server.StateMachine;
import com.abing.raft.server.entity.*;
import com.abing.raft.server.exception.RedirectException;
import com.abing.raft.server.rpc.HttpRpcServer;
import com.abing.raft.server.rpc.RpcServer;
import com.abing.raft.server.state.RuleStrategy;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * 默认的raft算法
 *
 * @author abing
 * @date 2023/10/11
 */
@Data
public class DefalutlRaft implements Raft {

    static Logger log = Logger.getLogger(DefalutlRaft.class.getName());

    /* ============  所有服务器上的易失性状态=============  */

    /**
     * 已知已提交的最高的日志条目的索引（初始值为0，单调递增）
     */
    private volatile long commitIndex = 0;

    /**
     * 已经被应用到状态机的最高的日志条目的索引（初始值为0，单调递增）
     */
    private volatile long lastApplied = 0;


    /* ============ 所有服务器上的持久性状态 =============  */

    /**
     * 服务器已知最新的任期（在服务器首次启动时初始化为0，单调递增）
     */
    private volatile long currentTerm = 0;

    public void incrCurrentTerm() {
        currentTerm++;
    }

    /**
     * 当前任期内收到选票的 candidateId，如果没有投给任何候选人 则为空
     */
    private String votedFor;

    public void votedForSelf() {
        votedFor = serverInfos.getSelf().getId();
    }

    /**
     * 日志条目集合；每个条目包含了用于状态机的命令，以及领导人接收到该条目时的任期（初始索引为1）
     */
    private LogModule logModule = new DefaultLogModule();

    private StateMachine stateMachine = new DefaultStateMachine();

    private RpcServer rpcServer = new HttpRpcServer(this);

    private ServerInfos serverInfos;

    private RuleStrategy rule;

    private long lastHearBeat;

    private ExecutorService executorService = Executors.newFixedThreadPool(3);

    public void hearBeat() {
        lastHearBeat = System.currentTimeMillis();
    }


    public DefalutlRaft(ServerInfos serverInfos) {
        this.serverInfos = serverInfos;
    }

@Override
    public void setRule(RuleStrategy rule) {
        this.rule = rule;
        this.rule.bind(this);
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
            rule.tryChangeRule(RuleStrategy.FOLLOWER);
        }
    }

    public LogEntry getLast() {
        return logModule.getLogEntry(commitIndex);
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

        // 更新领导id
        serverInfos.setLeader(appendEntry.getLeaderId());

        // 前一条日志是否存在
        LogEntry prevLogEntry = logModule.getLogEntry(appendEntry.getPrevLogIndex());
        if (prevLogEntry == null || !Objects.equals(prevLogEntry.getTerm(), appendEntry.getPreLogTerm())) {
            return AppendEntryResult.fail(currentTerm);
        }

        // 接收的日志与存在日志冲突
        List<LogEntry> entries = appendEntry.getEntries();
        if (CollectionUtils.isNotEmpty(entries)) {

            Iterator<LogEntry> iterator = entries.iterator();
            while (iterator.hasNext()) {
                LogEntry logEntry = iterator.next();
                LogEntry existLogEntry = logModule.getLogEntry(logEntry.getIndex());
                if (existLogEntry == null) {
                    break;
                }
                // 索引相同，任期不同，发生冲突 --> 删除存在的条目，及其以后的条目
                if (!Objects.equals(existLogEntry.getTerm(), logEntry.getTerm())) {
                    logModule.remove(logEntry.getIndex(), commitIndex);
                    break;
                }
                // 存在且不冲突的日志，不需要再次添加
                iterator.remove();
            }

            // 追加尚未存在的日志
            Long lastIndex = logModule.append(entries);

            long leaderCommit = appendEntry.getLeaderCommit();
            if (lastIndex != null && leaderCommit > commitIndex) {
                // 取 领导提交的最高日志索引 与 新条目的索引 最小值
                commitIndex = Math.min(leaderCommit, lastIndex);
            }
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
            LogEntry lastLog = logModule.getLogEntry(commitIndex);
            if (lastLog == null || lastLog.lessOrEqual(requestVote.getLastLogTerm(), requestVote.getLastLogIndex())) {
                votedFor = candidateId;

                return RequestVoteResult.approve(currentTerm);
            }
        }

        return RequestVoteResult.disapprove(currentTerm);
    }

    @Override
    public KvResult<?> clientKv(KvArgument<?> kvArgument) throws RedirectException {
        return rule.clientKv(kvArgument);
    }

    @Override
    public int getPort() {
        return serverInfos.getSelf().getPort();
    }

    public RuleStrategy nextRule;

    public String getId() {
        return serverInfos.getSelf().getId();
    }

    public String getNodeDesc() {
        return getId() + "[" + rule.display() + "]";
    }

    @Override
    public void start() {
        executorService.submit(new DefalutlRaft.NodeRuleCheck(this));
    }

    public static class NodeRuleCheck implements Runnable {
        DefalutlRaft defalutlRaft;

        public NodeRuleCheck(DefalutlRaft defalutlRaft) {
            this.defalutlRaft = defalutlRaft;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    log.info(MessageFormat.format("{0}  start", defalutlRaft.getNodeDesc()));

                    defalutlRaft.doSameThing();

                    log.info(MessageFormat.format("{0} change rule to [{1}]", defalutlRaft.getNodeDesc(), defalutlRaft.nextRule.display()));

                    // 设置新角色
                    defalutlRaft.setRule(defalutlRaft.nextRule);
                } catch (Exception e) {
                    log.warning(MessageFormat.format("{0} unknown error:  {1}", defalutlRaft.getNodeDesc(), e.getMessage()));

                    break;
                }
            }
        }
    }
}
