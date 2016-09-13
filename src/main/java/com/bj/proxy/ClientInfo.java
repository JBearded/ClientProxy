package com.bj.proxy;

/**
 * 客户端信息
 *
 * @author 谢俊权
 * @create 2016/5/6 10:22
 */
public class ClientInfo {

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
    private int weight;

    /**
     * 客户端实例
     */
    private ClosableClient client;

    /**
     * 客户端构造信息
     */
    private ClientConstructor clientConstructor;

    public ClientInfo(String ip, int port, int weight, ClientConstructor clientConstructor) {
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
        ClientInfo clientInfo = (ClientInfo) obj;
        return ip.equals(clientInfo.getIp())
                && port == clientInfo.getPort()
                && weight == clientInfo.getWeight();
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
