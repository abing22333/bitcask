package com.abing.raft.server.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author abing
 * @date 2023/10/10
 */
@Data
public class RequestVoteResult implements Serializable {
    /**
     * 当前任期号，以便于候选人去更新自己的任期号
     */
    long term;

    /**
     * 候选人赢得了此张选票时为 true
     */
    Boolean voteGranted;

    public static RequestVoteResult disapprove(long term) {
        RequestVoteResult result = new RequestVoteResult();
        result.setVoteGranted(false);
        result.setTerm(term);
        return result;
    }

    public static RequestVoteResult approve(long term) {
        RequestVoteResult result = new RequestVoteResult();
        result.setVoteGranted(true);
        result.setTerm(term);
        return result;
    }
}
