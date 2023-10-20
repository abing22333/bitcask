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

    /**
     * 获取最后的一个索引
     * @return long
     */
    Long getLastIndex();



    /**
     * 通过索引查询日志条目
     *
     * @param index 索引
     * @return LogEntry
     */
    LogEntry getLogEntry(long index);

    /**
     * 查询指定范围内的日志条目
     *
     * @param startIndex 开始索引(包含)
     * @param endIndex   结束索引(包含)
     * @return 日志条目集合
     */
    List<LogEntry> getLogEntry(long startIndex, long endIndex);

    /**
     * 追加日志条目
     *
     * @param logEntry 日志条目
     * @return 日志追加的最后一个位置
     */
    Long append(List<LogEntry> logEntry);

    /**
     * 删除指定范围内的所有日志条目
     *
     * @param startIndex 开始索引(包含)
     * @param endIndex   结束索引(包含)
     */
    void remove(long startIndex, long endIndex);
}
