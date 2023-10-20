package com.abing.raft.client;


import java.io.Serializable;

/**
 * @author abing
 * @date 2023/10/17
 */

public class KvResult<T> implements Serializable {

    Boolean success;


    T value;

    public Boolean getSuccess() {
        return success;
    }


    public T getValue() {
        return value;
    }

    public KvResult() {
    }

    public KvResult(boolean success, T value) {
        this.success = success;
        this.value = value;
    }

    public static KvResult<?> success(byte[] value) {
        return new KvResult<>(true, SerializationUtil.deserialize(value));
    }

    public static KvResult<?> success() {
        return new KvResult<>(true, null);
    }

    public static KvResult<?> fail() {
        return new KvResult<>(false, null);
    }
}

