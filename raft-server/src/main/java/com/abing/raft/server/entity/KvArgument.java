package com.abing.raft.server.entity;

import lombok.Data;

/**
 * @author abing
 * @date 2023/10/17
 */
@Data
public class KvArgument {
    String key;

    int command;
}
