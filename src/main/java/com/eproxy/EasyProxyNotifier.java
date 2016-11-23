package com.eproxy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 客户端代理的通知器
 * @author 谢俊权
 * @create 2016/5/11 17:57
 */
public class EasyProxyNotifier {

    private List<EasyProxy> clientProxies = new CopyOnWriteArrayList<EasyProxy>();

    private static class ServerProxyNotifierHolder{
        public static EasyProxyNotifier notifier = new EasyProxyNotifier();
    }
    private EasyProxyNotifier(){}

    public static EasyProxyNotifier getInstance(){
        return ServerProxyNotifierHolder.notifier;
    }

    /**
     * 注册需要被通知的客户端代理
     * @param easyProxy
     */
    public void subClientProxy(EasyProxy easyProxy){
        this.clientProxies.add(easyProxy);
    }

    /**
     * 通知客户端代理某个服务不可用
     * @param clientInfo
     */
    public void notifyServerAvailable(ClientInfo clientInfo){
        for(EasyProxy easyProxy : clientProxies){
            easyProxy.toAvailable(clientInfo);
        }
    }

    /**
     * 通知客户端代理某个服务不可用
     * @param clientInfo
     */
    public void notifyServerUnavailable(ClientInfo clientInfo){
        for(EasyProxy easyProxy : clientProxies){
            easyProxy.toUnavailable(clientInfo);
        }
    }
}
