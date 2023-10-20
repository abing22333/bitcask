package com.abing.raft.server.impl;

import com.abing.kv.bitcask.BitCaskFactory;
import com.abing.kv.bitcask.KvDataBase;
import com.abing.raft.client.SerializationUtil;
import com.abing.raft.server.LogModule;
import com.abing.raft.server.entity.LogEntry;
import org.apache.commons.collections4.CollectionUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author abing
 * @date 2023/10/10
 */
public class DefaultLogModule implements LogModule {
    KvDataBase bitCask = BitCaskFactory.create();


    private String getKey(long key) {
        return "log:" + key;
    }


    @Override
    public Long getLastIndex() {
        if (!bitCask.hasKey(getKey(1))) {
            return 0L;
        }

        long l = 1, r = Long.MAX_VALUE - 2, mid;

        while (l < r) {
            mid = (l + r + 1) / 2;
            if (bitCask.hasKey(getKey(mid))) {
                l = mid;
            } else {
                r = mid - 1;
            }
        }

        return l;
    }


    @Override
    public LogEntry getLogEntry(long index) {
        byte[] bytes = bitCask.get(getKey(index));
        return SerializationUtil.deserialize(bytes);
    }

    @Override
    public List<LogEntry> getLogEntry(long startIndex, long endIndex) {
        List<LogEntry> logEntries = new ArrayList<>((int) (endIndex - startIndex));

        while (startIndex <= endIndex) {
            LogEntry logEntry = getLogEntry(startIndex++);
            if (logEntry == null) {
                throw new RuntimeException(MessageFormat.format("index[{0}] is null", startIndex));
            }
            logEntries.add(logEntry);
        }
        return logEntries;
    }

    @Override
    public Long append(List<LogEntry> logEntries) {
        if (CollectionUtils.isEmpty(logEntries)) {
            return null;
        }
        long index = 0;

        for (LogEntry logEntry : logEntries) {
            index = logEntry.getIndex();
            bitCask.put(getKey(index), SerializationUtil.serialize(logEntry));
        }
        return index;
    }

    @Override
    public void remove(long startIndex, long endIndex) {
        while (startIndex <= endIndex) {
            bitCask.delete(getKey(startIndex++));
        }
    }
}
