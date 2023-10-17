package com.abing.raft.server.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author abing
 * @date 2023/10/10
 */
@Data
public class RequestVoteArgument  implements Serializable {
    /**
     * 候选人的任期号
     */
    private long term;

    /**
     * 请求选票的候选人的 ID
     */
    private String candidateId;

    /**
     * 候选人的最后日志条目的索引值
     */
    private long lastLogIndex;

    /**
     * 候选人最后日志条目的任期号
     */
    private long lastLogTerm;


    public static  RequestVoteArgumentBuilder builder(){
        return new RequestVoteArgumentBuilder();
    }

    public static final class RequestVoteArgumentBuilder {
        private long term;
        private String candidateId;
        private long lastLogIndex;
        private long lastLogTerm;

        private RequestVoteArgumentBuilder() {
        }

        public static RequestVoteArgumentBuilder aRequestVoteArgument() {
            return new RequestVoteArgumentBuilder();
        }

        public RequestVoteArgumentBuilder withTerm(long term) {
            this.term = term;
            return this;
        }

        public RequestVoteArgumentBuilder withCandidateId(String candidateId) {
            this.candidateId = candidateId;
            return this;
        }

        public RequestVoteArgumentBuilder withLastLogIndex(long lastLogIndex) {
            this.lastLogIndex = lastLogIndex;
            return this;
        }

        public RequestVoteArgumentBuilder withLastLogTerm(long lastLogTerm) {
            this.lastLogTerm = lastLogTerm;
            return this;
        }

        public RequestVoteArgument build() {
            RequestVoteArgument requestVoteArgument = new RequestVoteArgument();
            requestVoteArgument.setTerm(term);
            requestVoteArgument.setCandidateId(candidateId);
            requestVoteArgument.setLastLogIndex(lastLogIndex);
            requestVoteArgument.setLastLogTerm(lastLogTerm);
            return requestVoteArgument;
        }
    }
}
