package com.abing.raft.server.impl;

import com.abing.raft.server.StateMachine;
import com.abing.raft.server.entity.LogEntry;

/**
 * 默认状态机实现
 *
 * @author abing
 * @date 2023/10/17
 */
public class DefaultStateMachine implements StateMachine {
    @Override
    public void apply(LogEntry logEntry) {

    }
}
