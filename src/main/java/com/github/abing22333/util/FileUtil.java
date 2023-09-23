package com.github.abing22333.util;

import com.github.abing22333.db.Data;
import com.github.abing22333.db.Index;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 文件工具类
 *
 * @author abing
 * @date 2023/9/21
 */
public class FileUtil {

    public static byte[] read(FileChannel channel, Index index) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(index.getValueSize());
        channel.position(index.getValuePosition());
        buf.clear();
        channel.read(buf);
        return buf.array();
    }

    /**
     * 写入数据, 并返回写入数据的开始位置
     *
     * @param channel channel
     * @param data    数据
     * @return 数据的在文件中的位置
     */
    public static long write(FileChannel channel, Data data) throws IOException {
        long position = channel.position();
        ByteBuffer buf = ByteBuffer.wrap(data.getBytes());
        channel.write(buf);
        return position;
    }
}
