package com.eproxy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 客户端信息
 *
 * @author 谢俊权
 * @create 2016/5/6 10:22
 */
public class ServerInfo {

    /**
     * 服务ip
     */
    private String ip;

    /**
     * 服务端口
     */
    private int port;

    /**
     * 客户端权重
     */
    private int weight = 1;

    /**
     * 客户端实例
     */
    private ClosableClient client;

    private Map<String, Object> extendInfoMap = new HashMap<>();

    public ServerInfo(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public ServerInfo(String ip, int port, int weight) {
        this(ip, port);
        this.weight = weight;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public int getWeight() {
        return weight;
    }

    public ClosableClient getClient() {
        return client;
    }

    public void setClient(ClosableClient client) {
        this.client = client;
    }

    public void setExtendInfo(String key, Object value){
        this.extendInfoMap.put(key, value);
    }

    public Map<String, Object> getExtendInfoMap(){
        return Collections.unmodifiableMap(this.extendInfoMap);
    }


    @Override
    public boolean equals(Object obj) {
        ServerInfo serverInfo = (ServerInfo) obj;
        return ip.equals(serverInfo.getIp()) && port == serverInfo.getPort();
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + ip.hashCode();
        result = 37 * result + port;
        return result;
    }
}
