package com.abing.kv.bitcask;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author abing
 * @date 2023/9/23
 */
class RecordTest {


    @Test
    @DisplayName("Record序列化和反序列化")
    public void test() {

        Record record = new Record("abing", "hello bitcask".getBytes());
        ByteBuffer byteBuffer = record.toByteBuffer();
        int recodeLen = byteBuffer.flip().getInt();

        assertEquals(record.toString(), Record.from(byteBuffer).toString());
    }

    @Test
    public void testGetRecordLen() {

        String key = "abing";
        String value = "hello bitcask";

        Record record = new Record(key, value.getBytes());

        assertEquals(record.getRecordLen(), Record.FIXED_MATE_DATA_LENGTH + key.length() + value.getBytes().length);
    }


}