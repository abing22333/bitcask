package com.abing.raft.server.entity;

import com.abing.raft.client.KvArgument;
import com.abing.raft.client.SerializationUtil;
import lombok.Data;

import java.io.Serializable;

/**
 * 日志条目
 *
 * @author abing
 * @date 2023/10/10
 */
@Data
public class LogEntry implements Serializable {

    /**
     * 日志条目
     */
    long term;

    /**
     * 日志index
     */
    long index;

    /**
     * 指令的类型
     */
    Integer command;

    /**
     * 需要执行指令的key
     */
    String key;

    /**
     * 需要执行指令的value
     */
    byte[] value;

    public boolean lessOrEqual(long term, long index) {

        return this.term <= term && this.index <= index;
    }


    public static LogEntry createFrom(KvArgument<?> kvArgument, long term, long index) {
        LogEntry logEntry = new LogEntry();
        logEntry.setTerm(term);
        logEntry.setIndex(index);
        logEntry.setCommand(kvArgument.getCommand());
        logEntry.setKey(kvArgument.getKey());
        logEntry.setValue(SerializationUtil.serialize(kvArgument.getValue()));
        return logEntry;
    }
}
