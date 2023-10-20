package com.abing.raft.server.state;

import com.abing.raft.client.KvArgument;
import com.abing.raft.client.KvResult;
import com.abing.raft.client.ServerInfos;
import com.abing.raft.server.entity.AppendEntryArgument;
import com.abing.raft.server.entity.AppendEntryResult;
import com.abing.raft.server.entity.LogEntry;
import com.abing.raft.server.impl.DefalutlRaft;
import org.apache.commons.collections4.CollectionUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * 领导者角色
 *
 * @author abing
 * @date 2023/10/10
 */
public class LeaderStrategy extends RuleStrategy {
    static Logger log = Logger.getLogger(FollowerStrategy.class.getName());


    /* ========== 领导人上的易失性状态(选举后重新初始化) ================== */

    /**
     * 对于每一台服务器，发送到该服务器的下一个日志条目的索引（初始值为领导人最后的日志条目的索引+1）
     */
    Map<String, Long> nextIndexes;

    /**
     * 对于每一台服务器，已知的已经复制到该服务器的最高日志条目的索引（初始值为0，单调递增）
     */
    Map<String, Long> matchIndexes;


    @Override
    public void bind(DefalutlRaft defalutlRaft) {
        super.bind(defalutlRaft);
        int size = defalutlRaft.getServerInfos().getOtherNodeInfo().size();
        this.nextIndexes = new HashMap<>(size);
        this.matchIndexes = new HashMap<>(size);

        for (ServerInfos.ServerInfo serverInfo : defalutlRaft.getServerInfos().getOtherNodeInfo()) {
            nextIndexes.put(serverInfo.getId(), defalutlRaft.getCommitIndex() + 1);
            matchIndexes.put(serverInfo.getId(), 0L);
        }
    }

    @Override
    public KvResult<?> clientKv(KvArgument<?> kvArgument) {

        if (kvArgument.getCommand() == 0) {
            // get
            return KvResult.success(null);
        }
        Long lastIndex = defalutlRaft.getLogModule().getLastIndex();
        // add / del
        LogEntry logEntry = LogEntry.createFrom(kvArgument, defalutlRaft.getCurrentTerm(), lastIndex);

        Long index = defalutlRaft.getLogModule().append(List.of(logEntry));

        // 检测日志是否提交
        long start = System.currentTimeMillis();
        while (defalutlRaft.getCommitIndex() < index) {
            // 超时停止
            if (System.currentTimeMillis() - start >= 1000 * 2) {
                return KvResult.fail();
            }
        }

        return KvResult.success();
    }

    /**
     * 创建 AppendEntryArgument
     *
     * @param serverInfo 节点信息
     * @return AppendEntryArgument
     */
    AppendEntryArgument creatAppendEntryArgument(ServerInfos.ServerInfo serverInfo) {

        long nextIndex = nextIndexes.get(serverInfo.getId());

        List<LogEntry> entries = null;
        long preLogTerm = 0;
        Long lastIndex = defalutlRaft.getLogModule().getLastIndex();
        // lastLogIndex ≥ nextIndex
        if (lastIndex >= nextIndex) {
            // 发送日志
            entries = defalutlRaft.getLogModule().getLogEntry(nextIndex, lastIndex);
            preLogTerm = entries.stream().findFirst().orElseThrow().getTerm();
        } else {
            // 发送心跳
            LogEntry preLogEntry = defalutlRaft.getLast();
            if (preLogEntry != null) {
                preLogTerm = preLogEntry.getTerm();
            }
        }

        return AppendEntryArgument.builder()
                .withLeaderId(defalutlRaft.getId())
                .withLeaderCommit(defalutlRaft.getCommitIndex())
                .withTerm(defalutlRaft.getCurrentTerm())
                .withPreLogTerm(nextIndex - 1)
                .withPrevLogIndex(preLogTerm)
                .withEntries(entries)
                .build();
    }

    /**
     * 检查结果
     *
     * @param argument    请求参数
     * @param result      请求结果
     * @param serverInfo 节点信息
     */
    private void checkResult(AppendEntryArgument argument, AppendEntryResult result, ServerInfos.ServerInfo serverInfo) {

        defalutlRaft.checkTerm(result.getTerm());

        // 心跳结果，不需要检查
        if (CollectionUtils.isEmpty(argument.getEntries())) {
            return;
        }

        // 失败则 nextIndex 递减并重试
        if (!result.isSuccess()) {
            // nextIndex 递减
            nextIndexes.put(serverInfo.getId(), nextIndexes.get(serverInfo.getId()) - 1);
            return;
        }

        // 成功则更新 跟随者的 nextIndex 和 matchIndex
        LogEntry logEntry = argument.getEntries().get(argument.getEntries().size());
        long index = logEntry.getIndex();
        nextIndexes.put(serverInfo.getId(), index);
        matchIndexes.put(serverInfo.getId(), index);

        // 是否需要更新自身的 commitIndex
        if (index <= defalutlRaft.getCommitIndex()) {
            return;
        }

        // 多数的 matchIndex[i] ≥ N 以及 log[N].term == currentTerm 则令 commitIndex = N
        if (!Objects.equals(logEntry.getTerm(), defalutlRaft.getCurrentTerm())) {
            return;
        }

        int size = matchIndexes.values().size();
        long equalOrGreaterThanIndexCount = matchIndexes.values().stream().filter(matchIndex -> matchIndex >= index).count();
        if (equalOrGreaterThanIndexCount >= (size - 1) / 2) {
            defalutlRaft.setCommitIndex(index);
        }
    }


    /**
     * 给别的节点发送心跳
     */
    @Override
    public void doSameThing() {
        log.info(MessageFormat.format("{0}[Leader] start run", defalutlRaft.getId()));
        while (true) {
            if (isStop) {
                log.info(MessageFormat.format("{0}[Leader] end run", defalutlRaft.getId()));
                return;
            }

            // 节点信息
            ServerInfos.ServerInfo serverInfo = defalutlRaft.getServerInfos().nextMachineInfo();

            // 构造请求参数
            AppendEntryArgument argument = creatAppendEntryArgument(serverInfo);

            // 发送请求
            AppendEntryResult result = defalutlRaft.getRpcServer().appendEntry(argument, serverInfo);

            // 检查结果
            checkResult(argument, result, serverInfo);

            defalutlRaft.apply();
        }
    }


    @Override
    public String display() {
        return "LEADER";
    }
}
