package com.abing.raft.server.entity;

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
}
