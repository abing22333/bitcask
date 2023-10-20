package com.abing.raft.server;

import com.abing.raft.server.entity.LogEntry;

/**
 * 状态机接口
 *
 * @author abing
 * @date 2023/10/10
 */
public interface StateMachine {

    /**
     * 将日志应用到状态机中
     *
     * @param logEntry 日志条目
     * @return
     */
    void apply(LogEntry logEntry);
}
