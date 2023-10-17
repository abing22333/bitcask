package com.abing.raft.server.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author abing
 * @date 2023/10/10
 */
@Data
public class AppendEntryResult implements Serializable {
    /**
     * 当前任期，对于领导人而言 它会更新自己的任期
     */
    private long term;

    /**
     * 如果跟随者所含有的条目和 prevLogIndex 以及 prevLogTerm 匹配上了，则为 true
     */
    private Boolean success;

    public long getTerm() {
        return term;
    }

    public boolean isSuccess() {
        return success != null && success;
    }

    public AppendEntryResult() {
    }

    public AppendEntryResult(long term, Boolean success) {
        this.term = term;
        this.success = success;
    }

    public static AppendEntryResult fail(long term) {
        return new AppendEntryResult(term, false);
    }

    public static AppendEntryResult success(long term) {
        return new AppendEntryResult(term, true);
    }
}
