package com.github.abing22333.db;


import com.github.abing22333.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author abing
 * @date 2023/9/19
 */
public class BitCask {

    ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
    KeyDir keyDir;
    FileChannel writeChannel;
    FileChannel readChannel;
    static File file = new File("db-01.bin");

    public BitCask() {
        try {

            writeChannel = FileChannel.open(Paths.get(file.toURI()), StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.DSYNC);
            readChannel = FileChannel.open(Paths.get(file.toURI()), StandardOpenOption.READ);

            initKeyDir();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 初始化keyDir
     *
     * @throws IOException IOException
     */
    void initKeyDir() throws IOException {
        keyDir = new KeyDir();
        readChannel.position(0);

        while (true) {
            long filePosition = readChannel.position();
            buffer.clear();
            int read = readChannel.read(buffer);
            if (read == -1) {
                break;
            }

            int bufferPos = 0;
            while (bufferPos < read) {
                Data data = new Data();
                int dataPosition = bufferPos;

                bufferPos = Data.setMateData(data, buffer.array(), bufferPos);
                bufferPos = Data.setContext(data, buffer.array(), bufferPos);

                Index index = Index.from(data, filePosition + dataPosition);
                keyDir.put(new String(data.getKey()), index);
            }
        }
    }

    /**
     * 查询数据
     *
     * @param key key
     * @return value
     * @throws IOException IOException
     */
    public byte[] get(String key) throws IOException {
        Index index = keyDir.get(key);
        if (index == null) {
            return null;
        }

        FileChannel readChannel = FileChannel.open(Paths.get(file.toURI()), StandardOpenOption.READ);
        byte[] bytes = FileUtil.read(readChannel, index);

        readChannel.close();

        return bytes;
    }

    /**
     * 存储数据
     *
     * @param key   key
     * @param value value
     * @return true: 存储数据成功
     * @throws IOException IOException
     */
    public Boolean put(String key, byte[] value) throws IOException {

        Data data = new Data(key.getBytes(), value);

        long position = FileUtil.write(writeChannel, data);

        keyDir.put(key, Index.from(data, position));

        return true;
    }

    /**
     * 删除数据
     *
     * @param key key
     * @return true: 删除数据成功
     * @throws IOException IOException
     */
    public Boolean delete(String key) throws IOException {
        return put(key, new byte[0]);
    }

    public void clear() {
        keyDir.clear();
        try {
            writeChannel.force(true);
            writeChannel.close();
            readChannel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
