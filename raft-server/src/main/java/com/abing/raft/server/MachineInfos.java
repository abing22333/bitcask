package com.abing.raft.server;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 机器信息
 *
 * @author abing
 * @date 2023/10/11
 */

public class MachineInfos {


    /**
     * 地址： ip:port
     */
    Map<String, MachineInfo> otherNodeList = new HashMap<>();

    /**
     * 领导地址
     */
    MachineInfo leader;

    /**
     * 自己的地址
     */
    MachineInfo self;

    public MachineInfos(MachineInfo self) {
        this.self = self;
    }

    public Collection<MachineInfo> getOtherNodeInfo() {
        return otherNodeList.values();
    }

    public MachineInfo getLeader() {
        return leader;
    }

    public void add(MachineInfo machineInfo) {
        otherNodeList.put(machineInfo.getId(), machineInfo);
    }

    public void changeLeader(String id) {

        // todo changeLeader
    }

    public MachineInfo getSelf() {
        return self;
    }

    @Override
    public String toString() {
        return "MachineInfos{" +
               "otherNodeList=" + otherNodeList +
               ", leader=" + leader +
               ", self=" + self +
               '}';
    }

    public static class MachineInfo {
        /**
         * id
         */
        String id;

        String ip;

        int port;

        /**
         * @param str id@ip:port
         */
        public MachineInfo(String str) {

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

        public MachineInfo(String id, String addr) {
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


    public static MachineInfos createMachineInfos(String str) {
        if (str == null) {
            throw new RuntimeException("地址异常");
        }

        String[] addrArray = str.split(",");
        if (addrArray.length < 2) {
            throw new RuntimeException("地址异常");
        }
        MachineInfos machineInfos = new MachineInfos(new MachineInfo(addrArray[0]));

        Arrays.stream(addrArray).skip(1)
                .forEach(addr -> machineInfos.add(new MachineInfo(addr)));

        return machineInfos;
    }

}
