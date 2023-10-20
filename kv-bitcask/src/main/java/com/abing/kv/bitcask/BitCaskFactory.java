package com.abing.kv.bitcask;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author abing
 * @date 2023/10/10
 */
public class BitCaskFactory {

    public static KvDataBase create() {
        KeyDir keyDir = new KeyDir();
        try {
            initKeyDir(keyDir, "db-01.bin");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new BitCask(keyDir);
    }

    private static FileChannel openFile(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        if (!path.toFile().exists()){
            boolean newFile = path.toFile().createNewFile();
            if (!newFile){
                throw  new RuntimeException("cannot create file: " + fileName);
            }
        }

        return FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.CREATE);
    }



    private static void initKeyDir(KeyDir keyDir, String fileName) throws IOException {
        try (FileChannel channel = openFile(fileName)) {
            long filePosition = channel.position(), fileRead;
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);

            while ((fileRead = channel.read(buffer)) != -1) {
                buffer.flip();
                int bufferPosition = 0;

                while (buffer.hasRemaining()) {
                    // 读取recode长度
                    int recordLen = buffer.getInt();
                    // 缓存中数据不完整
                    if (buffer.remaining() < recordLen) {
                        break;
                    }

                    // 读取recode
                    Record record = Record.from(buffer);

                    Index index = Index.from(record, filePosition + bufferPosition);
                    keyDir.put(record.getKey(), index);
                    bufferPosition += record.getRecordLen();
                }
                // 压缩buffer中的残留数据
                buffer.compact();
                filePosition += fileRead;
            }
        }
    }
}
