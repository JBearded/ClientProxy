package com.bj.proxy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
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

    public void subClientProxy(ClientProxy clientProxy){
        this.clientProxies.add(clientProxy);
    }

    public void notifyServerAvailable(ServerInfo serverInfo){
        for(ClientProxy clientProxy : clientProxies){
            clientProxy.toAvailable(serverInfo);
        }
    }

    public void notifyServerUnavailable(ServerInfo serverInfo){
        for(ClientProxy clientProxy : clientProxies){
            clientProxy.toUnavailable(serverInfo);
        }
    }
}
