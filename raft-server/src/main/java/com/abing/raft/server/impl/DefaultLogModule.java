package com.abing.raft.server.impl;

import com.abing.kv.bitcask.BitCaskFactory;
import com.abing.kv.common.api.KvDataBase;
import com.abing.raft.server.LogModule;
import com.abing.raft.server.entity.LogEntry;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author abing
 * @date 2023/10/10
 */
public class DefaultLogModule implements LogModule {
    KvDataBase bitCask = BitCaskFactory.create();

    private final static String LastIndexKey = "LastIndexKey";


    @Override
    public long lastIndex() {
        byte[] bytes = bitCask.get(LastIndexKey);
        if (bytes == null || bytes.length == 0){
            return 0;
        }

        return ByteBuffer.wrap(bytes).getLong();
    }



    @Override
    public LogEntry getLast() {
        return null;
    }

    @Override
    public LogEntry getLogEntry(long index) {
        return null;
    }

    @Override
    public void write(LogEntry logEntry) {

    }

    @Override
    public void write(List<LogEntry> logEntry) {

    }

    @Override
    public void removeOnStartIndex(long index) {

    }
}
