package com.abing.raft.server;

import com.abing.raft.server.entity.LogEntry;

/**
 * 状态机接口
 * @author abing
 * @date 2023/10/10
 */
public interface StateMachine {
    void apply(LogEntry logEntry);

}
