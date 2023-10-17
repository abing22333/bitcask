package com.abing.raft.server;

import com.abing.raft.server.entity.LogEntry;

import java.util.List;

/**
 * 日志模块接口
 *
 * @author abing
 * @date 2023/10/10
 */
public interface LogModule {

    long lastIndex();

    LogEntry getLast();

    LogEntry getLogEntry(long index);

    void write(LogEntry logEntry);
    void write(List<LogEntry> logEntry);

    void removeOnStartIndex(long index);
}
