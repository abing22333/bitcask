package com.abing.bitcask.common.constant;

/**
 * 数据库http访问路径
 *
 * @author abing
 * @date 2023/9/27
 */
public interface DbHttpPaths {
    /**
     * 数据库获取数据
     */
    String DB_GET = "/db/get";
    /**
     * 数据库存储数据
     */
    String DB_PUT = "/db/put";
    /**
     * 数据库删除数据
     */
    String DB_DELETE = "/db/delete";
}
