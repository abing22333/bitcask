package com.abing.bitcask.db;

import java.util.HashMap;
import java.util.Map;

/**
 * 索引缓存
 *
 * @author abing
 * @date 2023/9/21
 */
public class KeyDir {
    Map<String, Index> cache = new HashMap<>();

    public void put(String key, Index value) {
        if (value.getValueSize() == 0) {
            cache.remove(key);
        } else {
            cache.put(key, value);
        }
    }

    public Index get(String key) {
        return cache.get(key);
    }

    public void clear() {
        cache.clear();
    }

    @Override
    public String toString() {
        return "KeyDir{" +
               "cache=" + cache +
               '}';
    }
}
