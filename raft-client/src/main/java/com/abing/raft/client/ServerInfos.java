package com.abing.raft.client;


import java.util.*;

/**
 * 维护其他节点信息
 *
 * @author abing
 * @date 2023/10/11
 */
public class ServerInfos {

    Map<Integer, ServerInfo> numNodeMap = new HashMap<>();

    /**
     * 地址： ip:port
     */
    Map<String, ServerInfo> idNodeMap = new HashMap<>();

    /**
     * 领导地址
     */
    ServerInfo leader;

    /**
     * 自己的地址
     */
    ServerInfo self;

    public ServerInfos(ServerInfo self) {
        this.self = self;
    }

    public Collection<ServerInfo> getOtherNodeInfo() {
        return Collections.unmodifiableCollection(idNodeMap.values());
    }

    public ServerInfo getLeader() {
        return leader;
    }

    public void add(ServerInfo serverInfo) {
        idNodeMap.put(serverInfo.getId(), serverInfo);
        numNodeMap.put(idNodeMap.size(), serverInfo);
    }

    public int nodeSize(){
        return idNodeMap.size();
    }

    private int nextNodeIndex;

    /**
     * 下一个节点的信息
     *
     * @return MachineInfo
     */
    public ServerInfo nextMachineInfo() {
        ServerInfo serverInfo = numNodeMap.get(nextNodeIndex);
        nextNodeIndex = (nextNodeIndex + 1) % numNodeMap.size();
        return serverInfo;
    }

    public void setLeader(String id) {
        leader = idNodeMap.get(id);
    }

    public ServerInfo getSelf() {
        return self;
    }

    @Override
    public String toString() {
        return "MachineInfos{" +
               "idNodeMap=" + idNodeMap +
               ", leader=" + leader +
               ", self=" + self +
               '}';
    }

    public static class ServerInfo {
        /**
         * id
         */
        String id;

        String ip;

        int port;

        /**
         * @param str id@ip:port
         */
        public ServerInfo(String str) {

            String[] infos = str.split("@");
            if (infos.length < 2) {
                throw new RuntimeException("地址异常");
            }

            this.id = infos[0];

            String[] addr = infos[1].split(":");
            if (addr.length > 1) {
                this.ip = addr[0];
                this.port = Integer.parseInt(addr[1]);
            } else {
                this.ip = "127.0.0.1";
                this.port = Integer.parseInt(addr[0]);
            }
        }

        public ServerInfo(String id, String addr) {
            this(id + "@" + addr);
        }


        public String getId() {
            return id;
        }

        public int getPort() {
            return port;
        }

        public String getAddr() {
            return ip + ":" + port;
        }

        @Override
        public String toString() {
            return "MachineInfo{" +
                   "id='" + id + '\'' +
                   ", ip='" + ip + '\'' +
                   ", port=" + port +
                   '}';
        }
    }


    public static ServerInfos createServerInfos(String str) {
        if (str == null) {
            throw new RuntimeException("地址异常");
        }

        String[] addrArray = str.split(",");
        if (addrArray.length < 2) {
            throw new RuntimeException("地址异常");
        }
        ServerInfos serverInfos = new ServerInfos(new ServerInfo(addrArray[0]));

        Arrays.stream(addrArray).skip(1)
                .forEach(addr -> serverInfos.add(new ServerInfo(addr)));

        return serverInfos;
    }

}
