package com.abing.bitcask.db;

import com.abing.bitcask.common.api.BitCask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author abing
 * @date 2023/9/27
 */
class BitCaskImplTest {

    @Test
    void getAndPut() {
        BitCask bitCask = BitCaskFactory.create();
        byte[] value1 = "value-getAndPut-001".getBytes();
        byte[] value2 = "value-getAndPut-002".getBytes();
        byte[] value3 = "value-getAndPut-003".getBytes();
        bitCask.put("key1", value1);
        bitCask.put("key2", value2);
        bitCask.put("key3", value3);

        bitCask = BitCaskFactory.create();
        assertArrayEquals(value1, bitCask.get("key1"));
        assertArrayEquals(value2, bitCask.get("key2"));
        assertArrayEquals(value3, bitCask.get("key3"));
    }


    @Test
    void delete() {
        BitCask bitCask = BitCaskFactory.create();
        byte[] value1 = "value-delete-001".getBytes();
        bitCask.put("key1", value1);
        assertArrayEquals(value1, bitCask.get("key1"));

        bitCask.delete("key1");
        assertNull(bitCask.get("key1"));
    }
}