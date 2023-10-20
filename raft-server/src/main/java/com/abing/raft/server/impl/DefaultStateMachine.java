package com.abing.raft.server.impl;

import com.abing.kv.bitcask.BitCaskFactory;
import com.abing.kv.bitcask.KvDataBase;
import com.abing.raft.server.StateMachine;
import com.abing.raft.server.entity.LogEntry;

/**
 * 默认状态机实现
 *
 * @author abing
 * @date 2023/10/17
 */
public class DefaultStateMachine implements StateMachine {
    KvDataBase bitCask = BitCaskFactory.create();

    private String getKey(String key) {
        return "state:" + key;
    }

    @Override
    public void apply(LogEntry logEntry) {
        if (logEntry.getCommand() == 0){
            byte[] bytes = bitCask.get(logEntry.getKey());
        }
        if (logEntry.getCommand() == 1){

        }
        bitCask.put(getKey(logEntry.getKey()), logEntry.getValue());
    }




}
