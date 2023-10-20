package com.abing.raft.server.state;

import com.abing.raft.server.impl.DefalutlRaft;

import java.text.MessageFormat;
import java.util.logging.Logger;

/**
 * 跟随着角色
 *
 * @author abing
 * @date 2023/10/10
 */
public class FollowerStrategy extends RuleStrategy {
    static Logger log = Logger.getLogger(FollowerStrategy.class.getName());



    private final static long TIME_OUT = 6 * 1000;


    /**
     * 检查心跳是否超时
     */
    @Override
    public void doSameThing() {
        log.info(MessageFormat.format("{0}[Follower] start run", defalutlRaft.getId()));
        while (true) {
            if (isStop) {
                log.info(MessageFormat.format("{0}[Follower] end run", defalutlRaft.getId()));
                return;
            }

            if (isTimeOut()) {
                // 心跳超时，变成候选者
                tryChangeRule(RuleStrategy.CANDIDATE);
            }
        }
    }

    @Override
    public void bind(DefalutlRaft defalutlRaft) {
        super.bind(defalutlRaft);
        defalutlRaft.hearBeat();
    }

    @Override
    public String display() {
        return "FOLLOWER";
    }



    private boolean isTimeOut() {

        if (System.currentTimeMillis() - defalutlRaft.getLastHearBeat() > TIME_OUT) {
            defalutlRaft.hearBeat();
            return true;
        }
        return false;
    }
}
