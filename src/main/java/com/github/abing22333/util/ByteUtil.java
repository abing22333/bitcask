package com.github.abing22333.util;


/**
 * 字节转换工具
 *
 * @author abing
 * @date 2023/9/21
 */
public class ByteUtil {

    public static byte[] toByteArray(short value) {
        byte[] bytes = new byte[Short.BYTES];
        for (int i = 0; i < Short.BYTES; i++) {
            int shift = (Short.BYTES - i - 1) * Byte.SIZE;
            bytes[i] = (byte) ((value >> shift) & 0xFF);
        }
        return bytes;
    }

    public static byte[] toByteArray(int value) {
        byte[] bytes = new byte[Integer.BYTES];
        for (int i = 0; i < Integer.BYTES; i++) {
            int shift = (Integer.BYTES - i - 1) * Byte.SIZE;
            bytes[i] = (byte) ((value >> shift) & 0xFF);
        }
        return bytes;
    }

    public static byte[] toByteArray(long value) {
        byte[] bytes = new byte[Long.BYTES];
        for (int i = 0; i < Long.BYTES; i++) {
            int shift = (Long.BYTES - i - 1) * Byte.SIZE;
            bytes[i] = (byte) ((value >> shift) & 0xFF);
        }
        return bytes;
    }

    public static short byteArrayToShort(byte[] bytes, int pos) {

        short value = 0;
        for (int i = 0; i < Short.BYTES; i++) {
            int shift = (Short.BYTES - i - 1) * Byte.SIZE;
            value += (bytes[i + pos] & 0xFF) << shift;
        }
        return value;
    }

    public static int byteArrayToInt(byte[] bytes, int pos) {

        int value = 0;
        for (int i = 0; i < Integer.BYTES; i++) {
            int shift = (Integer.BYTES - i - 1) * Byte.SIZE;
            value += (bytes[i + pos] & 0xFF) << shift;
        }
        return value;
    }

    public static long byteArrayToLong(byte[] bytes, int pos) {
        long value = 0;
        for (int i = 0; i < Long.BYTES; i++) {
            int shift = (Long.BYTES - i - 1) * Byte.SIZE;
            value += (long) (bytes[i + pos] & 0xFF) << shift;
        }
        return value;
    }
}
