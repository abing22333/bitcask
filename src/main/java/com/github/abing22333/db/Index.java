package com.github.abing22333.db;

/**
 * 索引
 *
 * @author abing
 * @date 2023/9/21
 */
public class Index {

    /**
     * 文件名称
     */
    private String fileId;
    /**
     * 内容的长度
     */
    private int valueSize;

    /**
     * 内容在文件中的偏移量
     */
    private long valuePosition;

    /**
     * 时间戳
     */
    private long timestamp;

    public String getFileId() {
        return fileId;
    }

    public Index setFileId(String fileId) {
        this.fileId = fileId;
        return this;
    }

    public int getValueSize() {
        return valueSize;
    }

    public Index setValueSize(int valueSize) {
        this.valueSize = valueSize;
        return this;
    }

    public long getValuePosition() {
        return valuePosition;
    }

    public Index setValuePosition(long valuePosition) {
        this.valuePosition = valuePosition;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Index setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public String toString() {
        return "Index{" +
               "fileId='" + fileId + '\'' +
               ", valueSize=" + valueSize +
               ", valuePosition=" + valuePosition +
               ", timestamp=" + timestamp +
               '}';
    }

    public static Index from(Data data, long filePosition) {
        Index index = new Index();
        index.setTimestamp(data.getTimestamp());
        index.setValueSize(data.getValueSize());
        index.setValuePosition(data.getValuePosition() + filePosition);
        return index;
    }
}
