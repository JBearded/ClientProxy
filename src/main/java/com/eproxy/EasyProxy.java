package com.eproxy;

import com.eproxy.loadbalance.*;
import com.eproxy.utils.TelnetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 客户端代理
 * @author 谢俊权
 * @create 2016/5/6 10:26
 */
public class EasyProxy<T>{

    private static final Logger logger = LoggerFactory.getLogger(EasyProxy.class);

    /**
     * 检查服务是否可用的定时器
     */
    private Timer timer;

    /**
     * 代理服务的配置信息
     */
    private Configure configure;

    /**
     * 可用的服务客户端信息列表
     */
    private CopyOnWriteArrayList<ServerInfo> availableServers = new CopyOnWriteArrayList<ServerInfo>();

    /**
     * 不可用的服务客户端信息列表
     */
    private CopyOnWriteArrayList<ServerInfo> unavailableServers = new CopyOnWriteArrayList<ServerInfo>();

    /**
     * 负载均衡调度
     */
    private LoadBalance loadBalance;

    /**
     * 负载均衡策略
     */
    private LoadBalanceStrategy strategy;

    /**
     * 客户端工厂
     */
    private ClientFactory clientFactory;


    public EasyProxy(ServerInfoResolver resolver) {
        this(resolver.get(), new Configure.Builder().build());
    }

    public EasyProxy(List<ServerInfo> serverInfoList) {
        this(serverInfoList, new Configure.Builder().build());
    }

    public EasyProxy(ServerInfoResolver resolver, Configure configure) {
        this(resolver.get(), configure);
    }

    public EasyProxy(List<ServerInfo> serverInfoList, Configure configure) {
        this.init(serverInfoList, configure);
    }

    /**
     * 初始化
     * @param serverInfoList 服务客户端信息列表
     * @param configure 配置信息
     */
    protected void init(List<ServerInfo> serverInfoList, Configure configure){
        this.configure = configure;
        this.strategy = configure.getLoadBalanceStrategy();
        this.clientFactory = new ClientFactory(configure);
        this.initClientInfo(serverInfoList);
        this.initLoadBalance();
        this.scheduleCheckServerAvailable();
        EasyProxyNotifier.getInstance().subClientProxy(this);
    }

    /**
     * 根据配置, 初始化调度策略
     */
    protected void initLoadBalance() {
        int size = this.availableServers.size();
        switch (strategy){
            case RR:
                this.loadBalance = new RRLoadBalance(size);
                break;
            case WRR:
                int[] weights = new int[size];
                int index = 0;
                for(ServerInfo serverInfo : availableServers){
                    weights[index] = serverInfo.getWeight();
                    index++;
                }
                this.loadBalance = new WRRLoadBalance(weights);
                break;
            case HASH:
                this.loadBalance = new HashLoadBalance(size);
                break;
        }
    }

    /**
     * 初始化服务信息
     * @param serverInfoList 服务信息列表
     */
    protected void initClientInfo(List<ServerInfo> serverInfoList) {
        if(serverInfoList != null && !serverInfoList.isEmpty()){
            this.availableServers.addAll(serverInfoList);
        }
    }

    /**
     * 定时检查不可用的服务是否已经恢复
     */
    protected void scheduleCheckServerAvailable(){
        this.timer = new Timer();
        long delayMS = 1000;
        long intervalMS = configure.getCheckServerAvailableIntervalMs();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long timeoutMS = configure.getTelnetTimeoutMs();
                for (ServerInfo serverInfo : unavailableServers) {
                    if(TelnetUtil.isConnect(serverInfo.getIp(), serverInfo.getPort(), timeoutMS)){
                        EasyProxyNotifier.getInstance().notifyServerAvailable(serverInfo);
                    }
                }
            }
        }, delayMS, intervalMS);
    }

    /**
     * 通过调度策略获取可用的客户端, 此方法一般用于非hash策略, 如果使用的是hash策略, 则永远获取第一个可用的客户端
     * @return
     */
    public T getClient() {
        int index = this.loadBalance.getIndex(null);
        return (T) getClient(index);
    }

    /**
     * 通过调度策略获取可用的客户端, 此方法一般用于hash策略, 如果使用其他的策略, 则无视key
     * @param key hash的key值
     * @return
     */
    public T getClient(String key) {
        int index = this.loadBalance.getIndex(key);
        return (T) getClient(index);
    }

    /**
     * 根据索引获取可用的客户端
     * @param index 索引
     * @return
     */
    protected ClosableClient getClient(int index){
        if(this.availableServers.isEmpty()){
            logger.error("available server list is empty");
            return null;
        }
        ServerInfo serverInfo = availableServers.get(index);
        ClosableClient client = serverInfo.getClient();
        if(client == null){
            client = create(serverInfo);
        }
        return client;
    }

    /**
     * 根据服务信息创建一个新的客户端
     * @param serverInfo 服务端信息
     * @return
     */
    protected ClosableClient create(ServerInfo serverInfo){
        ClosableClient client = clientFactory.create(serverInfo);
        serverInfo.setClient(client);
        return client;
    }

    /**
     * 在设置服务不可用后, 销毁客户端的连接
     * @param serverInfo 服务端信息
     */
    protected void destroy(ServerInfo serverInfo){
        ClosableClient client = serverInfo.getClient();
        client.close();
        serverInfo.setClient(null);
    }

    /**
     * 恢复服务为可用
     * @param serverInfo 服务客户端信息
     */
    protected void toAvailable(ServerInfo serverInfo) {
        if(this.unavailableServers.contains(serverInfo)){
            create(serverInfo);
            this.availableServers.add(serverInfo);
            this.unavailableServers.remove(serverInfo);
            this.initLoadBalance();
        }
    }

    /**
     * 设置服务为不可用
     * @param serverInfo 服务客户端信息
     */
    protected void toUnavailable(ServerInfo serverInfo) {
        if(this.availableServers.contains(serverInfo)) {
            this.destroy(serverInfo);
            this.availableServers.remove(serverInfo);
            this.unavailableServers.add(serverInfo);
            this.initLoadBalance();
        }
    }


}
