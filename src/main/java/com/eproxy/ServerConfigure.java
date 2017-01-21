package com.eproxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiejunquan
 * @create 2017/1/13 14:50
 */
public class ServerConfigure {

    private String zookeeperHosts;
    private long groupId;
    private String serverName;
    private Map<String, String> extendInfo = new HashMap<>();
    private List<ServerInfo> serverInfoList = new ArrayList<>();


    public void setZookeeperHosts(String zookeeperHosts) {
        this.zookeeperHosts = zookeeperHosts;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    public void setServerInfoList(List<ServerInfo> serverInfoList) {
        this.serverInfoList = serverInfoList;
    }

    public String getZookeeperHosts() {
        return zookeeperHosts;
    }

    public long getGroupId() {
        return groupId;
    }

    public String getServerName() {
        return serverName;
    }

    public List<ServerInfo> getServerInfoList() {
        return serverInfoList;
    }

    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }
}
