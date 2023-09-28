# 介绍

基于[bitcask论文](https://riak.com/assets/bitcask-intro.pdf)实现的的key/value数据库，C/S架构，使用http通信。

# 运行

## 原生方式运行
要求： java 11+

1. package
```bash
mvn clean package -DskipTests=true
```

2. 运行
```bash
 java -jar bitcask-db-server/target/bitcask-db-server.jar
```

3. 测试

存储数据

```bash
curl -X POST --location "http://127.0.0.1:8080/db/put?key=abing" -d "this is value"
```

获取数据

```bash
curl -X POST --location "http://127.0.0.1:8080/db/get?key=abing"
```

删除数据


```bash
curl -X POST --location "http://127.0.0.1:8080/db/delete?key=abing"
```

