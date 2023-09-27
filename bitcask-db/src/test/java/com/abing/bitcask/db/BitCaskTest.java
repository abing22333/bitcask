package com.abing.bitcask.db;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author abing
 * @date 2023/9/27
 */
class BitCaskTest {

    @Test
    void getAndPut() {
        BitCask bitCask = new BitCask();
        byte[] value1 = "value-getAndPut-001".getBytes();
        byte[] value2 = "value-getAndPut-002".getBytes();
        byte[] value3 = "value-getAndPut-003".getBytes();
        bitCask.put("key1", value1);
        bitCask.put("key2", value2);
        bitCask.put("key3", value3);
        bitCask.clear();

        bitCask = new BitCask();
        assertArrayEquals(value1, bitCask.get("key1"));
        assertArrayEquals(value2, bitCask.get("key2"));
        assertArrayEquals(value3, bitCask.get("key3"));
        bitCask.clear();
    }


    @Test
    void delete() {
        BitCask bitCask = new BitCask();
        byte[] value1 = "value-delete-001".getBytes();
        bitCask.put("key1", value1);
        assertArrayEquals(value1, bitCask.get("key1"));

        bitCask.delete("key1");
        assertNull(bitCask.get("key1"));

        bitCask.clear();
    }
}