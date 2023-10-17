package com.abing.kv.common.api;

/**
 * kv数据库接口
 *
 * @author abing
 * @date 2023/9/26
 */
public interface KvDataBase {

    /**
     * 查询数据
     *
     * @param key key
     * @return byte[]
     */
    byte[] get(String key);

    /**
     * 存储数据
     *
     * @param key   key
     * @param value value
     */
    void put(String key, byte[] value);

    /**
     * 删除数据
     *
     * @param key key
     */
    void delete(String key);
}
