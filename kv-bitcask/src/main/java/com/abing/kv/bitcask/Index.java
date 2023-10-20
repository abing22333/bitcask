package com.abing.kv.bitcask;

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
    private Integer valueSize;
    /**
     * 内容的位置
     */
    private Long valuePosition;

    /**
     * 时间戳
     */
    private Long timestamp;

    public String getFileId() {
        return fileId;
    }

    public Index setFileId(String fileId) {
        this.fileId = fileId;
        return this;
    }


    public long getTimestamp() {
        return timestamp;
    }

    public Index setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Integer getValueSize() {
        return valueSize;
    }

    public Index setValueSize(Integer valueSize) {
        this.valueSize = valueSize;
        return this;
    }

    public Long getValuePosition() {
        return valuePosition;
    }

    public Index setValuePosition(Long valuePosition) {
        this.valuePosition = valuePosition;
        return this;
    }

    public Index setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * 是否删除标识
     *
     * @return boolean
     */
    public boolean isDelete() {
        return valueSize != 0;
    }

    public static Index from(Record record, long filePosition) {
        Index index = new Index();
        index.setTimestamp(record.getTimestamp());
        index.setValueSize(record.getValueSize());
        index.setValuePosition(filePosition + (record.getRecordLen() - record.getValueSize()));
        return index;
    }
}
