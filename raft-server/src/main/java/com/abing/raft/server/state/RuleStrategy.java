package com.abing.raft.server.state;

import com.abing.raft.client.KvArgument;
import com.abing.raft.client.KvResult;
import com.abing.raft.client.ServerInfos;
import com.abing.raft.server.exception.RedirectException;
import com.abing.raft.server.impl.DefalutlRaft;

/**
 * @author abing
 * @date 2023/10/17
 */
public abstract class RuleStrategy {

    public static final RuleStrategy CANDIDATE = new CandidateStrategy();
    public static final RuleStrategy LEADER = new LeaderStrategy();
    public static final RuleStrategy FOLLOWER = new FollowerStrategy();

    protected volatile boolean isStop;
    protected DefalutlRaft defalutlRaft;


    public boolean isLeader() {
        return LEADER.display().equals(display());
    }


    public KvResult<?> clientKv(KvArgument<?> kvArgument) {
        ServerInfos.ServerInfo leader = defalutlRaft.getServerInfos().getLeader();
        throw new RedirectException(leader.getAddr());
    }


    public void stop() {
        isStop = true;
    }


    public boolean isStop() {
        return isStop;
    }


    public void bind(DefalutlRaft defalutlRaft) {
        this.defalutlRaft = defalutlRaft;
        this.isStop = false;
        this.defalutlRaft.setVotedFor(null);
    }


    public void tryChangeRule(RuleStrategy targetRule) {
        if (isStop()) {
            return;
        }
        if (targetRule.display().equals(display())) {
            return;
        }
        defalutlRaft.setNextRule(targetRule);
        stop();
    }

   public abstract String display();

    public  abstract   void doSameThing();
}
