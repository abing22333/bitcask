package com.abing.raft.server.state;

/**
 * @author abing
 * @date 2023/10/17
 */
public abstract class NodeRule implements Rule {
    protected volatile boolean isStop;
    protected RaftNode raftNode;


    @Override
    public void stop() {
        isStop = true;
    }

    @Override
    public boolean isStop() {
        return isStop;
    }

    @Override
    public void bind(RaftNode raftNode) {
        this.raftNode = raftNode;
        this.isStop = false;
        this.raftNode.votedFor = null;
    }

    @Override
    public void tryChangeRule(Rule targetRule) {
        if (isStop()) {
            return;
        }
        if (targetRule.display().equals(display())) {
            return;
        }

        raftNode.nextRule = targetRule;
        stop();
    }
}
