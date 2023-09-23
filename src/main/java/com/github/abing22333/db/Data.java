package com.github.abing22333.db;

import com.github.abing22333.util.ByteUtil;

import java.util.Arrays;

/**
 * 数据在文件中的结构
 *
 * @author abing
 * @date 2023/9/21
 */
public class Data {
    /**
     * 元数据长度：version， crc, timestamp, keySize, valueSize
     */
    public static final int MATE_DATA_SIZE = Byte.BYTES + Long.BYTES + Long.BYTES + Short.BYTES + Integer.BYTES;

    /**
     * 数据格式版本
     */
    private final byte version = 0;

    /**
     * Cyclic Redundancy Check
     */
    private long crc;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * key大小
     */
    private short keySize;

    /**
     * value大小
     */
    private int valueSize;

    /**
     * key
     */
    private byte[] key;

    /**
     * value
     */
    private byte[] value;

    public Data() {
    }

    public Data(long timestamp, byte[] key, byte[] value) {
        this.timestamp = timestamp;
        this.key = key;
        this.value = value;
        this.keySize = (short) key.length;
        this.valueSize = value.length;
    }

    public Data(byte[] key, byte[] value) {
        this(System.currentTimeMillis(), key, value);
    }

    public long getCrc() {
        return crc;
    }

    public Data setCrc(long crc) {
        this.crc = crc;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Data setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public int getKeySize() {
        return keySize;
    }

    public Data setKeySize(short keySize) {
        this.keySize = keySize;
        return this;
    }

    public int getValueSize() {
        return valueSize;
    }

    public Data setValueSize(int valueSize) {
        this.valueSize = valueSize;
        return this;
    }

    public byte[] getKey() {
        return key;
    }

    public Data setKey(byte[] key) {
        this.key = key;
        return this;
    }

    public byte[] getValue() {
        return value;
    }

    public Data setValue(byte[] value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return "Data{" +
               "version=" + version +
               ", crc=" + crc +
               ", timestamp=" + timestamp +
               ", keySize=" + keySize +
               ", valueSize=" + valueSize +
               ", key=" + Arrays.toString(key) +
               ", value=" + Arrays.toString(value) +
               '}';
    }

    public byte[] getBytes() {
        int destPos = 0;
        byte[] bytes = new byte[MATE_DATA_SIZE + keySize + valueSize];

        System.arraycopy(ByteUtil.toByteArray(version), 0, bytes, destPos, Byte.BYTES);
        destPos += Byte.BYTES;
        System.arraycopy(ByteUtil.toByteArray(crc), 0, bytes, destPos, Long.BYTES);
        destPos += Long.BYTES;
        System.arraycopy(ByteUtil.toByteArray(timestamp), 0, bytes, destPos, Long.BYTES);
        destPos += Long.BYTES;
        System.arraycopy(ByteUtil.toByteArray(keySize), 0, bytes, destPos, Short.BYTES);
        destPos += Short.BYTES;
        System.arraycopy(ByteUtil.toByteArray(valueSize), 0, bytes, destPos, Integer.BYTES);
        destPos += Integer.BYTES;
        System.arraycopy(key, 0, bytes, destPos, key.length);
        destPos += key.length;
        System.arraycopy(value, 0, bytes, destPos, value.length);

        return bytes;
    }

    /**
     * 获取value在当前数据中的位置
     *
     * @return long
     */
    public long getValuePosition() {
        return MATE_DATA_SIZE + keySize;
    }

    public static Data form(byte[] bytes) {

        return form(bytes, 0);
    }

    /**
     * 从bytes的指定位置，给data中的元数据赋值
     *
     * @param data data
     * @param bytes bytes
     * @param startPos 指定位置
     * @return data使用的偏移量
     */
    public static int setMateData(Data data, byte[] bytes, int startPos) {
        // 跳过version
        int pos = startPos + Byte.BYTES;

        data.setCrc(ByteUtil.byteArrayToLong(bytes, pos));
        pos += Long.BYTES;
        data.setTimestamp(ByteUtil.byteArrayToLong(bytes, pos));
        pos += Long.BYTES;
        data.setKeySize(ByteUtil.byteArrayToShort(bytes, pos));
        pos += Short.BYTES;
        data.setValueSize(ByteUtil.byteArrayToInt(bytes, pos));
        pos += Integer.BYTES;
        return pos;
    }

    public static int setContext(Data data, byte[] bytes, int startPos) {

        int pos = startPos;

        data.setKey(Arrays.copyOfRange(bytes, pos, pos + data.getKeySize()));
        pos += data.getKeySize();

        data.setValue(Arrays.copyOfRange(bytes, pos, pos + data.getValueSize()));
        pos += data.getValueSize();

        return pos;
    }

    public static Data form(byte[] bytes, int startPos) {
        Data data = new Data();

        int pos = setMateData(data, bytes, startPos);

        setContext(data, bytes, pos);

        return data;
    }
}
