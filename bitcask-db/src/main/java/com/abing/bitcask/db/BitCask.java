package com.abing.bitcask.db;


import com.abing.bitcask.common.util.CloseUtil;

import java.io.File;
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
public class BitCask implements com.abing.bitcask.common.api.BitCask {

    KeyDir keyDir;

    static File file = new File("db-01.bin");

    FileChannel getWriteChannel() throws IOException {
        return FileChannel.open(Paths.get(file.toURI()), StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.DSYNC);
    }


    FileChannel getReadChannel() throws IOException {
        return FileChannel.open(Paths.get(file.toURI()), StandardOpenOption.READ);
    }

    public BitCask() {
        try {
            keyDir = new KeyDir();
            initKeyDir();
        } catch (IOException ignored) {

        }
    }

    /**
     * 初始化keyDir
     *
     * @throws IOException IOException
     */
    private void initKeyDir() throws IOException {

        FileChannel readChannel = getReadChannel();

        while (true) {
            long filePosition = readChannel.position();

            // 读取数据长度
            ByteBuffer recordLenBuf = ByteBuffer.allocate(Integer.BYTES);
            int read = readChannel.read(recordLenBuf);
            if (read == -1) {
                break;
            }
            recordLenBuf.flip();
            int recordLen = recordLenBuf.getInt();

            // 读取recode内容
            ByteBuffer recordValueBuf = ByteBuffer.allocate(recordLen - Integer.BYTES);
            read = readChannel.read(recordValueBuf);
            if (read == -1) {
                break;
            }
            recordValueBuf.flip();

            Record record = Record.from(recordValueBuf);

            Index index = Index.from(record, filePosition);
            keyDir.put(record.getKey(), index);
        }

        CloseUtil.close(readChannel);
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
        try {
            FileChannel readChannel = getReadChannel();
            readChannel.position(index.getValuePosition());

            ByteBuffer valueBuf = ByteBuffer.allocate(index.getValueSize());
            readChannel.read(valueBuf);

            readChannel.close();
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

        try {
            FileChannel writeChannel = getWriteChannel();
            long position = writeChannel.position();

            ByteBuffer buffer = record.toByteBuffer();
            buffer.flip();

            writeChannel.write(buffer);
            writeChannel.force(true);
            writeChannel.close();

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

    public void clear() {
        keyDir.clear();
    }
}
