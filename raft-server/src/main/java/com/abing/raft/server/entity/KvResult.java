package com.abing.raft.server.entity;

import lombok.Data;

/**
 * @author abing
 * @date 2023/10/17
 */
@Data
public class KvResult<T> {
    boolean success;

    T data;
}

