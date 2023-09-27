package com.abing.bitcask.db;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 文件中的数据记录
 *
 * @author abing
 * @date 2023/9/21
 */
public class Record {
    /**
     * 固定元数据长度：recordLen，timestamp, keySize, valueSize 4 + 8 + 2 + 4 = 18
     */
    public static final int FIXED_MATE_DATA_LENGTH = Integer.BYTES + Long.BYTES + Short.BYTES + Integer.BYTES;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * key大小
     */
    private Short keySize;

    /**
     * value大小
     */
    private Integer valueSize;

    /**
     * key
     */
    private String key;

    /**
     * value
     */
    private byte[] value;

    public Record() {
    }

    public Record(long timestamp, String key, byte[] value) {
        this.timestamp = timestamp;
        this.key = key;
        this.value = value;
        this.keySize = (short) key.length();
        this.valueSize = value.length;
    }

    public Record(String key, byte[] value) {
        this(System.currentTimeMillis(), key, value);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Record setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Short getKeySize() {
        return keySize;
    }

    public Record setKeySize(short keySize) {
        this.keySize = keySize;
        return this;
    }

    public Integer getValueSize() {
        return valueSize;
    }

    public Record setValueSize(int valueSize) {
        this.valueSize = valueSize;
        return this;
    }

    public String getKey() {
        return key;
    }

    public Record setKey(String key) {
        this.key = key;
        return this;
    }

    public byte[] getValue() {
        return value;
    }

    public Record setValue(byte[] value) {
        this.value = value;
        return this;
    }

    public int getRecordLen() {
        return FIXED_MATE_DATA_LENGTH + keySize + valueSize;
    }

    @Override
    public String toString() {
        return "Data{" + ", timestamp=" + timestamp + ", keySize=" + keySize + ", valueSize=" + valueSize + ", key=" + key + ", value=" + Arrays.toString(value) + '}';
    }


    public ByteBuffer toByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(getRecordLen());
        buffer.putInt(getRecordLen());
        buffer.putLong(getTimestamp());
        buffer.putShort(getKeySize());
        buffer.putInt(getValueSize());
        buffer.put(getKey().getBytes());
        buffer.put(getValue());

        return buffer;
    }

    /**
     * 从buffer中解析出Record
     *
     * @param buffer        buffer
     * @param loadRecordLen 是否从buffer读取'记录总长度'
     * @return Record
     */
    public static Record from(ByteBuffer buffer, boolean loadRecordLen) {
        Record record = new Record();
        if (loadRecordLen) {
            // 记录的总长度
            buffer.getInt();
        }

        record.setTimestamp(buffer.getLong());
        record.setKeySize(buffer.getShort());
        record.setValueSize(buffer.getInt());

        byte[] key = new byte[record.getKeySize()];
        buffer.get(key);
        record.setKey(new String(key));
        byte[] value = new byte[record.getValueSize()];
        buffer.get(value);
        record.setValue(value);

        return record;

    }


    /**
     * 从buffer中解析出Record, 不会从buffer中读取记录总长度
     *
     * @param buffer buffer
     * @return Record
     */
    public static Record from(ByteBuffer buffer) {
        return from(buffer, false);
    }
}
