package com.abing.raft.server;

import com.abing.raft.server.entity.*;
import com.abing.raft.server.state.RaftNode;
import com.abing.raft.server.state.Rule;
import lombok.Data;

import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * 服务器节点
 *
 * @author abing
 * @date 2023/10/10
 */
@Data
public class Raft {
    static Logger log = Logger.getLogger(Raft.class.getName());

    RaftNode raftNode;

    MachineInfos machineInfos;


    public Raft(String arg) {
        machineInfos = MachineInfos.createMachineInfos(arg);
        raftNode = new RaftNode(machineInfos);
        raftNode.setRule(Rule.FOLLOWER);
        init();
    }

    ExecutorService executorService = Executors.newFixedThreadPool(2);

    void init() {
        executorService.submit(new NodeRuleCheck());
    }

    public AppendEntryResult appendEntry(AppendEntryArgument appendEntry) {
        log.info(MessageFormat.format("{0}  receive appendEntry: [{1}]", raftNode.getNodeInfo(), appendEntry.toString()));
        AppendEntryResult result = raftNode.appendEntry(appendEntry);
        log.info(MessageFormat.format("{0}  response appendEntry: [{1}]", raftNode.getNodeInfo(), result.toString()));
        return result;
    }


    public RequestVoteResult requestVote(RequestVoteArgument requestVote) {
        log.info(MessageFormat.format("{0}  receive requestVote: [{1}]", raftNode.getNodeInfo(), requestVote.toString()));
        RequestVoteResult result = raftNode.requestVote(requestVote);
        log.info(MessageFormat.format("{0}  response requestVote: [{1}]", raftNode.getNodeInfo(), result.toString()));
        return result;
    }


    public KvResult<?> kv(KvArgument argument) {
        // todo
        return new KvResult<>();
    }

    public class NodeRuleCheck implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    log.info(MessageFormat.format("{0}[{1}] ", raftNode.getNodeInfo(), raftNode.getRule().display()));

                    raftNode.doSameThing();

                    log.info(MessageFormat.format("{0} change rule to [{1}]", raftNode.getNodeInfo(), raftNode.nextRule.display()));

                    // 设置新角色
                    raftNode.setRule(raftNode.nextRule);
                } catch (Exception e) {
                    log.warning(MessageFormat.format("{0}[{1}] error ", raftNode.getNodeInfo(), raftNode.getRule().display()));
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    public int getPort() {
        return machineInfos.getSelf().getPort();
    }
}
