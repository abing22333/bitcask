package com.abing.bitcask.common.api;

/**
 * bitcask接口
 *
 * @author abing
 * @date 2023/9/26
 */
public interface BitCask {
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

    /**
     * 清除缓存
     */
    void clear();
}
