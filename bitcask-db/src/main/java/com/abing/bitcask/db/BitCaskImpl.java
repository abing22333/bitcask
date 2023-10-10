package com.abing.bitcask.db;


import com.abing.bitcask.common.api.BitCask;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * bitcask实现
 *
 * @author abing
 * @date 2023/9/19
 */
public class BitCaskImpl implements BitCask {
    /**
     * 文件名暂时固定
     */
    private static final String FILE_NAME = "db-01.bin";

    private final KeyDir keyDir;


    private FileChannel getWriteChannel(String fileName) throws IOException {
        return FileChannel.open(Paths.get(fileName), StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.DSYNC);
    }


    private FileChannel getReadChannel(String fileName) throws IOException {
        return FileChannel.open(Paths.get(fileName), StandardOpenOption.READ);
    }

    public BitCaskImpl() {
        keyDir = new KeyDir();
    }

    protected BitCaskImpl(KeyDir keyDir) {
        this.keyDir = keyDir;
    }

    /**
     * 查询数据
     *
     * @param key key
     * @return value
     */
    @Override
    public byte[] get(String key) {
        Index index = keyDir.get(key);
        if (index == null) {
            return null;
        }
        try(FileChannel readChannel = getReadChannel(FILE_NAME)) {
            readChannel.position(index.getValuePosition());

            ByteBuffer valueBuf = ByteBuffer.allocate(index.getValueSize());
            readChannel.read(valueBuf);

            return valueBuf.array();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 存储数据
     *
     * @param key   key
     * @param value value
     */
    @Override
    public void put(String key, byte[] value) {

        Record record = new Record(key, value);

        try(FileChannel writeChannel = getWriteChannel(FILE_NAME)) {
            long position = writeChannel.position();

            ByteBuffer buffer = record.toByteBuffer();
            buffer.flip();

            writeChannel.write(buffer);
            writeChannel.force(true);

            keyDir.put(key, Index.from(record, position));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除数据
     *
     * @param key key
     */
    @Override
    public void delete(String key) {
        put(key, new byte[0]);
    }

    @Override
    public void clear() {
        keyDir.clear();
    }
}
