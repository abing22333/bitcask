package com.abing.raft.server.state;

/**
 * @author abing
 * @date 2023/10/17
 */
public interface Rule {
    void doSameThing();

    void bind(RaftNode raftNode);

    String display();

    void stop();

    boolean isStop();

    /**
     * 尝试切换角色
     * @param targetRule 目标角色
     */
    void tryChangeRule(Rule targetRule);

    Rule CANDIDATE = new CandidateRaft();
    Rule LEADER = new LeaderRaft();
    Rule FOLLOWER = new FollowerRaft();
}
