# raft 介绍

 
基于[raft论文](https://ramcloud.atlassian.net/wiki/download/attachments/6586375/raft.pdf)实现的分布式key/value数据库，节点之前通信使用http

实现功能：
1. 领导人选举 (完成)
2. 日志复制 (未实现)
3. 集群成员变化 (未实现)
4. 日志压缩 (未实现)


##  运行

### 原生方式运行

要求：java 11+

1. package

```bash
mvn clean package -DskipTests=true
```

```bash
cd raft-server
```

2. 运行

```bash
java -jar /target/raft-service.jar  node2@8082,node1@8081,node3@8083,
```

```bash
java -jar /target/raft-service.jar  node1@8081,node2@8082,node3@8083,
```

3. 结果

可以观察到节点之间的选举过程：

 
