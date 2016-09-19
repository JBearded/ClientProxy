package com.client.proxy;

import java.util.List;

/**
 * 服务的客户端信息
 * @author 谢俊权
 * @create 2016/9/2 15:59
 */
public abstract class ClientInfoResolver {

    protected String configPath;

    public ClientInfoResolver(String configPath) {
        this.configPath = configPath;
    }

    public abstract List<ClientInfo> get();
}
