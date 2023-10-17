package com.abing.raft.server.state;

import java.text.MessageFormat;
import java.util.logging.Logger;

/**
 * 跟随着角色
 *
 * @author abing
 * @date 2023/10/10
 */
public class FollowerRaft extends NodeRule {
    static Logger log = Logger.getLogger(FollowerRaft.class.getName());



    private final static long TIME_OUT = 6 * 1000;


    /**
     * 检查心跳是否超时
     */
    @Override
    public void doSameThing() {
        log.info(MessageFormat.format("{0}[Follower] start run", raftNode.getId()));
        while (true) {
            if (isStop) {
                log.info(MessageFormat.format("{0}[Follower] end run", raftNode.getId()));
                return;
            }

            if (isTimeOut()) {
                // 心跳超时，变成候选者
                tryChangeRule(Rule.CANDIDATE);
            }
        }
    }

    @Override
    public void bind(RaftNode raftNode) {
        super.bind(raftNode);
        raftNode.lastHearBeat = -1;
    }

    @Override
    public String display() {
        return "FOLLOWER";
    }



    private boolean isTimeOut() {
        if (raftNode.lastHearBeat == -1) {
            raftNode.lastHearBeat = System.currentTimeMillis();
            return false;
        }
        if (System.currentTimeMillis() - raftNode.lastHearBeat > TIME_OUT) {
            raftNode.lastHearBeat = System.currentTimeMillis();
            return true;
        }
        return false;
    }
}
