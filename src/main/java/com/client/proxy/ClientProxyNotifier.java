package com.client.proxy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 客户端代理的通知器
 * @author 谢俊权
 * @create 2016/5/11 17:57
 */
public class ClientProxyNotifier {

    private List<ClientProxy> clientProxies = new CopyOnWriteArrayList<ClientProxy>();

    private static class ServerProxyNotifierHolder{
        public static ClientProxyNotifier notifier = new ClientProxyNotifier();
    }
    private ClientProxyNotifier(){}

    public static ClientProxyNotifier getInstance(){
        return ServerProxyNotifierHolder.notifier;
    }

    /**
     * 注册需要被通知的客户端代理
     * @param clientProxy
     */
    public void subClientProxy(ClientProxy clientProxy){
        this.clientProxies.add(clientProxy);
    }

    /**
     * 通知客户端代理某个服务不可用
     * @param clientInfo
     */
    public void notifyServerAvailable(ClientInfo clientInfo){
        for(ClientProxy clientProxy : clientProxies){
            clientProxy.toAvailable(clientInfo);
        }
    }

    /**
     * 通知客户端代理某个服务不可用
     * @param clientInfo
     */
    public void notifyServerUnavailable(ClientInfo clientInfo){
        for(ClientProxy clientProxy : clientProxies){
            clientProxy.toUnavailable(clientInfo);
        }
    }
}
