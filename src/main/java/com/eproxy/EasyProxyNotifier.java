package com.eproxy;

import java.util.List;

/**
 * 客户端代理的通知器
 * @author 谢俊权
 * @create 2016/5/11 17:57
 */
public class EasyProxyNotifier {

    private EasyProxy proxy;

    public EasyProxyNotifier(EasyProxy easyProxy){
        this.proxy = easyProxy;
    }

    /**
     * 通知客户端代理某个服务不可用
     * @param serverInfo
     */
    public void serverAvailable(ServerInfo serverInfo){
        proxy.toAvailable(serverInfo);
    }

    /**
     * 通知客户端代理某个服务不可用
     * @param serverInfo
     */
    public void serverUnavailable(ServerInfo serverInfo){
        proxy.toUnavailable(serverInfo);
    }

    public void updateAvailableServers(List<ServerInfo> list){
        proxy.initClientInfo(list);
    }
}
