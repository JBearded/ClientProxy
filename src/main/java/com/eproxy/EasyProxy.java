package com.eproxy;

import com.eproxy.loadbalance.*;
import com.eproxy.utils.TelnetUtil;
import com.eproxy.zookeeper.ZookeeperClient;
import com.eproxy.zookeeper.ZookeeperHostsGetter;
import net.sf.cglib.proxy.Enhancer;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 客户端代理
 * @author 谢俊权
 * @create 2016/5/6 10:26
 */
public abstract class EasyProxy<T>{

    private static final Logger logger = LoggerFactory.getLogger(EasyProxy.class);

    private static final ReentrantLock reentrantLock = new ReentrantLock(false);

    /**
     * 检查服务是否可用的定时器
     */
    private Timer timer;

    /**
     * 代理服务的配置信息
     */
    private ProxyConfigure proxyConfigure;

    private ServerConfigure serverConfigure;

    /**
     * 可用的服务客户端信息列表
     */
    private List<ServerInfo> availableServers = new ArrayList<>();

    /**
     * 不可用的服务客户端信息列表
     */
    private List<ServerInfo> unavailableServers = new ArrayList<>();

    /**
     * 负载均衡调度
     */
    private LoadBalance loadBalance;

    private EasyProxyNotifier proxyNotifier;

    private ZookeeperClient zookeeperClient;


    public EasyProxy(String config) {
        this(config, new ProxyConfigure.Builder().build());
    }

    public EasyProxy(String config, ProxyConfigure proxyConfigure) {
        this.init(config, proxyConfigure);
    }

    /**
     * 初始化
     * @param config 服务客户端信息列表
     * @param pConfig 配置信息
     */
    private void init(String config, ProxyConfigure pConfig){
        this.proxyNotifier = EasyProxyNotifier.getInstance();
        this.proxyConfigure = pConfig;
        this.serverConfigure = ServerConfigureResolver.get(config);
        this.initClientInfo(serverConfigure.getServerInfoList());
        this.initZookeeper();
        this.proxyNotifier.subClientProxy(this);
        this.scheduleCheckServerAvailable();
    }

    /**
     * 初始化服务信息
     * @param serverInfoList 服务信息列表
     */
    private void initClientInfo(List<ServerInfo> serverInfoList) {
        reentrantLock.lock();
        try{
            if(serverInfoList != null && !serverInfoList.isEmpty()){
                this.unavailableServers.clear();
                this.availableServers.clear();
                this.availableServers.addAll(serverInfoList);
                this.initLoadBalance();
            }
        }finally {
            reentrantLock.unlock();
        }
    }

    private void initZookeeper(){
        this.zookeeperClient = new ZookeeperClient(proxyConfigure, serverConfigure);
        this.zookeeperClient.asyncServerData();
    }

    /**
     * 根据配置, 初始化调度策略
     */
    private void initLoadBalance() {
        int size = this.availableServers.size();
        LoadBalanceStrategy strategy = this.proxyConfigure.getLoadBalanceStrategy();
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
     * 定时检查不可用的服务是否已经恢复
     */
    private void scheduleCheckServerAvailable(){
        this.timer = new Timer();
        long delayMS = 1000;
        long intervalMS = proxyConfigure.getCheckServerAvailableIntervalMs();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (ServerInfo serverInfo : unavailableServers) {
                    if(TelnetUtil.isConnect(serverInfo.getIp(), serverInfo.getPort(), proxyConfigure.getTelnetTimeoutMs())){
                        proxyNotifier.notifyServerAvailable(serverInfo);
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
    private ClosableClient getClient(int index){
        reentrantLock.lock();
        try{
            if(this.availableServers.isEmpty()){
                logger.error("available server list is empty");
                return null;
            }
            ServerInfo serverInfo = availableServers.get(index);
            ClosableClient client = serverInfo.getClient();
            if(client == null){
                client = createProxyClient(serverInfo);
                serverInfo.setClient(client);
            }
            return client;
        }finally {
            reentrantLock.unlock();
        }
    }

    /**
     * 根据服务信息创建一个新的客户端
     * @param serverInfo 服务端信息
     * @return
     */
    protected abstract ClosableClient create(ServerInfo serverInfo);

    protected ClosableClient createProxyClient(ServerInfo serverInfo){
        ClosableClient client = create(serverInfo);
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(client.getClass());
        ClientInterceptor handler = new ClientInterceptor(client, serverInfo, proxyConfigure);
        enhancer.setCallback(handler);
        ClosableClient proxyClient = (ClosableClient) enhancer.create();
        return proxyClient;
    }

    /**
     * 在设置服务不可用后, 销毁客户端的连接
     * @param serverInfo 服务端信息
     */
    protected void destroy(ServerInfo serverInfo){
        ClosableClient client = serverInfo.getClient();
        if(client != null){
            client.close();
        }
        serverInfo.setClient(null);
    }

    /**
     * 恢复服务为可用
     * @param serverInfo 服务客户端信息
     */
     void toAvailable(ServerInfo serverInfo) {
         reentrantLock.lock();
         try{
             if(!this.availableServers.contains(serverInfo)){
                 ClosableClient client = createProxyClient(serverInfo);
                 serverInfo.setClient(client);
                 this.availableServers.add(serverInfo);
                 this.unavailableServers.remove(serverInfo);
                 this.initLoadBalance();
             }
         }finally {
             reentrantLock.unlock();
         }
    }

    /**
     * 设置服务为不可用
     * @param serverInfo 服务客户端信息
     */
    void toUnavailable(ServerInfo serverInfo) {
        reentrantLock.lock();
        try{
            if(this.availableServers.contains(serverInfo)) {
                this.destroy(serverInfo);
                this.availableServers.remove(serverInfo);
                this.unavailableServers.add(serverInfo);
                this.initLoadBalance();
            }
        }finally {
            reentrantLock.unlock();
        }
    }


}
