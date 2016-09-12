package com.bj.proxy;

/**
 * 服务器信息
 *
 * @author 谢俊权
 * @create 2016/5/6 10:22
 */
public class ServerInfo {

    private String ip;

    private int port;

    private int weight;

    private ClosableClient client;

    private ClientConstructor clientConstructor;

    public ServerInfo(String ip, int port, int weight, ClientConstructor clientConstructor) {
        this.ip = ip;
        this.port = port;
        this.weight = weight;
        this.clientConstructor = clientConstructor;
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

    public ClientConstructor getClientConstructor() {
        return clientConstructor;
    }

    @Override
    public boolean equals(Object obj) {
        ServerInfo serverInfo = (ServerInfo) obj;
        return ip.equals(serverInfo.getIp())
                && port == serverInfo.getPort()
                && weight == serverInfo.getWeight();
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + ip.hashCode();
        result = 37 * result + port;
        result = 37 * result + weight;
        return result;
    }
}
