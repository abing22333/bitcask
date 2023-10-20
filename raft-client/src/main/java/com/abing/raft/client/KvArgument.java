package com.abing.raft.client;


import java.io.Serializable;

/**
 * @author abing
 * @date 2023/10/17
 */

public class KvArgument<T> implements Serializable {
    String key;

    T value;

    int command;

    public KvArgument() {
    }

    public String getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }

    public int getCommand() {
        return command;
    }


}
