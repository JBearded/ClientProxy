package com.eproxy;

import com.eproxy.loadbalance.*;
import com.eproxy.utils.TelnetUtil;
import com.eproxy.zookeeper.ZookeeperClient;
import net.sf.cglib.proxy.Enhancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 客户端代理
 * @author 谢俊权
 * @create 2016/5/6 10:26
 */
public abstract class EasyProxy<T>{

    private static final Logger logger = LoggerFactory.getLogger(EasyProxy.class);

    private static final ReadWriteLock lock = new ReentrantReadWriteLock(false);

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
        this.proxyNotifier = new EasyProxyNotifier(this);
        this.proxyConfigure = pConfig;
        this.serverConfigure = ServerConfigureResolver.get(config);
        this.initClientInfo(serverConfigure.getServerInfoList());
        this.initZookeeper();
        this.scheduleCheckServerAvailable();
    }

    /**
     * 初始化服务信息
     * @param serverInfoList 服务信息列表
     */
    void initClientInfo(List<ServerInfo> serverInfoList) {
        lock.writeLock().lock();
        try{
            if(serverInfoList != null && !serverInfoList.isEmpty()){
                for(ServerInfo serverInfo : availableServers){
                    if(!serverInfoList.contains(serverInfo)){
                        destroy(serverInfo);
                        availableServers.remove(serverInfo);
                    }
                }
                for(ServerInfo serverInfo : serverInfoList){
                    if(!availableServers.contains(serverInfo)){
                        availableServers.add(serverInfo);
                    }
                }
                unavailableServers.clear();
                initLoadBalance();
            }
        }finally {
            lock.writeLock().unlock();
        }
    }

    private void initZookeeper(){
        this.zookeeperClient = new ZookeeperClient(proxyNotifier, proxyConfigure, serverConfigure);
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
                List<ServerInfo> list = new ArrayList<>();
                lock.readLock().lock();
                try{
                    for (ServerInfo serverInfo : unavailableServers) {
                        ServerInfo newServerInfo = new ServerInfo(serverInfo.getIp(), serverInfo.getPort());
                        list.add(newServerInfo);
                    }
                }finally {
                    lock.readLock().unlock();
                }
                for (ServerInfo serverInfo : list) {
                    if(TelnetUtil.isConnect(serverInfo.getIp(), serverInfo.getPort(), proxyConfigure.getTelnetTimeoutMs())){
                        lock.readLock().lock();
                        try{
                            int index = unavailableServers.indexOf(serverInfo);
                            ServerInfo originServerInfo = unavailableServers.get(index);
                            if(originServerInfo != null){
                                proxyNotifier.serverAvailable(originServerInfo);
                            }
                        }finally {
                            lock.readLock().unlock();
                        }
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
        lock.readLock().lock();
        ServerInfo serverInfo = null;
        try{
            if(availableServers.isEmpty()){
                logger.error("available server list is empty");
                return null;
            }
            serverInfo = availableServers.get(index);
        }finally {
            lock.readLock().unlock();
        }
        ClosableClient client = serverInfo.getClient();
        if(client == null){
            client = createProxyClient(serverInfo);
            serverInfo.setClient(client);
        }
        return client;
    }

    /**
     * 根据服务信息创建一个新的客户端
     * @param serverInfo 服务端信息
     * @return
     */
    protected abstract ClosableClient create(ServerInfo serverInfo);

    protected ClosableClient createProxyClient(ServerInfo serverInfo){
        try{
            ClosableClient client = create(serverInfo);
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(client.getClass());
            ClientInterceptor handler = new ClientInterceptor(client, proxyNotifier, serverInfo, proxyConfigure);
            enhancer.setCallback(handler);
            return (ClosableClient) enhancer.create();
        }catch (Exception e){
            logger.error("error to create proxy client, info:{}", serverInfo, e);
        }
        return null;
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
         lock.writeLock().lock();
         try{
             if(unavailableServers.contains(serverInfo)){
                 unavailableServers.remove(serverInfo);
             }
             if(!availableServers.contains(serverInfo)){
                 ClosableClient client = createProxyClient(serverInfo);
                 serverInfo.setClient(client);
                 if(client != null){
                     availableServers.add(serverInfo);
                     initLoadBalance();
                 }
             }
         }finally {
             lock.writeLock().unlock();
         }
    }

    /**
     * 设置服务为不可用
     * @param serverInfo 服务客户端信息
     */
    void toUnavailable(ServerInfo serverInfo) {
        lock.writeLock().lock();
        try{
            if(!unavailableServers.contains(serverInfo)){
                unavailableServers.add(serverInfo);
            }
            if(this.availableServers.contains(serverInfo)) {
                destroy(serverInfo);
                availableServers.remove(serverInfo);
                initLoadBalance();
            }
        }finally {
            lock.writeLock().unlock();
        }
    }


}
