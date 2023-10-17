package com.abing.raft.server.state;

import com.abing.raft.server.MachineInfos;
import com.abing.raft.server.entity.AppendEntryArgument;
import com.abing.raft.server.entity.AppendEntryResult;
import com.abing.raft.server.entity.LogEntry;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * 领导者角色
 *
 * @author abing
 * @date 2023/10/10
 */
public class LeaderRaft extends NodeRule {
    static Logger log = Logger.getLogger(FollowerRaft.class.getName());


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
    public void bind(RaftNode raftNode) {
        super.bind(raftNode);
        int size = raftNode.machineInfos.getOtherNodeInfo().size();
        this.nextIndexes = new HashMap<>(size);
        this.matchIndexes = new HashMap<>(size);

        for (MachineInfos.MachineInfo machineInfo : raftNode.machineInfos.getOtherNodeInfo()) {
            nextIndexes.put(machineInfo.getId(), raftNode.logModule.lastIndex() + 1);
            matchIndexes.put(machineInfo.getId(), 0L);
        }
    }

    public void appendEntry() {

        Collection<MachineInfos.MachineInfo> otherNodeInfo = raftNode.machineInfos.getOtherNodeInfo();

        for (MachineInfos.MachineInfo machineInfo : otherNodeInfo) {
            if (isStop) {
                return;
            }

            long prevLogIndex = nextIndexes.get(machineInfo.getId()) - 1;
            long preLogTerm = 0;
            List<LogEntry> entries = null;
            if (prevLogIndex > 0) {
                LogEntry preLogEntry = raftNode.logModule.getLogEntry(prevLogIndex);
                preLogTerm = preLogEntry.getTerm();
                entries = List.of(preLogEntry);
            }

            AppendEntryArgument argument = AppendEntryArgument.builder()
                    .withLeaderId(raftNode.getId())
                    .withLeaderCommit(raftNode.commitIndex)
                    .withTerm(raftNode.currentTerm)
                    .withPreLogTerm(prevLogIndex)
                    .withPrevLogIndex(preLogTerm)
                    .withEntries(entries)
                    .build();

            log.info(MessageFormat.format("{0}  send appendEntry: [{1}]", raftNode.getNodeInfo(), argument.toString()));
            AppendEntryResult result = raftNode.rpcServer.appendEntry(argument, machineInfo);
            log.info(MessageFormat.format("{0}  receive appendEntry: [{1}]", raftNode.getNodeInfo(), result.toString()));

            raftNode.checkTerm(result.getTerm());

            if (result.isSuccess()) {

            } else {

            }
        }
    }


    /**
     * 给别的节点发送心跳
     */
    @Override
    public void doSameThing() {
        log.info(MessageFormat.format("{0}[Leader] start run", raftNode.getId()));
        while (true) {
            if (isStop) {
                log.info(MessageFormat.format("{0}[Leader] end run", raftNode.getId()));
                return;
            }

            appendEntry();
        }
    }


    @Override
    public String display() {
        return "LEADER";
    }


    /**
     * 假设存在 N 满足 N > commitIndex，使得大多数的 matchIndex[i] ≥ N以及log[N].term == currentTerm成立
     *
     * @return N
     */
    Long findN() {

        List<Long> matchIndexList = new ArrayList<>(matchIndexes.values());

        Collections.sort(matchIndexList);

        for (int i = (matchIndexList.size() - 1) / 2; i > 0; i--) {
            Long N = matchIndexList.get(i);
            if (N < raftNode.commitIndex) {
                return null;
            }

            if (Objects.equals(raftNode.logModule.getLogEntry(N).getTerm(), raftNode.currentTerm)) {
                return N;
            }
        }
        return null;
    }

}
