package com.abing.raft.server.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 追加条目
 *
 * @author abing
 * @date 2023/10/10
 */
@Data
public class AppendEntryArgument implements Serializable {


    /** 领导人的任期  */
    private long term;

    /** 领导人的 Id，以便于跟随者重定向请求 */
    private String leaderId;

    /**紧邻新日志条目之前的那个日志条目的索引  */
    private long prevLogIndex;

    /** 紧邻新日志条目之前的那个日志条目的任期  */
    private long preLogTerm;

    /** 需要被保存的日志条目（被当做心跳使用时，则日志条目内容为空；为了提高效率可能一次性发送多个） */
    private List<LogEntry> entries;

    /** 领导人的已知已提交的最高的日志条目的索引  */
    private long leaderCommit;


    public static   AppendEntryArgumentBuilder builder(){
        return AppendEntryArgumentBuilder.anAppendEntryArgument();
    }




    public static final class AppendEntryArgumentBuilder {
        private long term;
        private String leaderId;
        private long prevLogIndex;
        private long preLogTerm;
        private List<LogEntry> entries;
        private long leaderCommit;

        private AppendEntryArgumentBuilder() {
        }

        public static AppendEntryArgumentBuilder anAppendEntryArgument() {
            return new AppendEntryArgumentBuilder();
        }

        public AppendEntryArgumentBuilder withTerm(long term) {
            this.term = term;
            return this;
        }

        public AppendEntryArgumentBuilder withLeaderId(String leaderId) {
            this.leaderId = leaderId;
            return this;
        }

        public AppendEntryArgumentBuilder withPrevLogIndex(long prevLogIndex) {
            this.prevLogIndex = prevLogIndex;
            return this;
        }

        public AppendEntryArgumentBuilder withPreLogTerm(long preLogTerm) {
            this.preLogTerm = preLogTerm;
            return this;
        }

        public AppendEntryArgumentBuilder withEntries(List<LogEntry> entries) {
            this.entries = entries;
            return this;
        }

        public AppendEntryArgumentBuilder withLeaderCommit(long leaderCommit) {
            this.leaderCommit = leaderCommit;
            return this;
        }

        public AppendEntryArgument build() {
            AppendEntryArgument appendEntryArgument = new AppendEntryArgument();
            appendEntryArgument.setTerm(term);
            appendEntryArgument.setLeaderId(leaderId);
            appendEntryArgument.setPrevLogIndex(prevLogIndex);
            appendEntryArgument.setPreLogTerm(preLogTerm);
            appendEntryArgument.setEntries(entries);
            appendEntryArgument.setLeaderCommit(leaderCommit);
            return appendEntryArgument;
        }
    }
}
