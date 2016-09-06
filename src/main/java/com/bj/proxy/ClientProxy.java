package com.bj.proxy;

import com.bj.loadbalance.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author 谢俊权
 * @create 2016/5/6 10:26
 */
public class ClientProxy<T>{

    private static final Logger logger = LoggerFactory.getLogger(ClientProxy.class);

    private Timer timer;

    private Configure configure;

    private CopyOnWriteArrayList<ServerInfo> availableServers = new CopyOnWriteArrayList<ServerInfo>();
    private CopyOnWriteArrayList<ServerInfo> unavailableServers = new CopyOnWriteArrayList<ServerInfo>();

    private LoadBalance loadBalance;

    private LoadBalanceStrategy strategy;

    private ClientProxyFactory clientProxyFactory;


    public ClientProxy(ServerInfoResolver resolver) {
        this(resolver.get(), new Configure.Builder().build());
    }

    public ClientProxy(List<ServerInfo> serverInfoList) {
        this(serverInfoList, new Configure.Builder().build());
    }

    public ClientProxy(ServerInfoResolver resolver, Configure configure) {
        this(resolver.get(), configure);
    }

    public ClientProxy(List<ServerInfo> serverInfoList, Configure configure) {
        this.init(serverInfoList, configure);
    }

    protected void init(List<ServerInfo> serverInfoList, Configure configure){
        this.configure = configure;
        this.strategy = configure.getLoadBalanceStrategy();
        this.clientProxyFactory = new ClientProxyFactory(configure);
        this.initServerInfo(serverInfoList);
        this.initLoadBalance();
        this.scheduleCheckServerAvailable();
        ClientProxyNotifier.getInstance().subClientProxy(this);
    }

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

    protected void initServerInfo(List<ServerInfo> serverInfoList) {
        if(serverInfoList != null && !serverInfoList.isEmpty()){
            this.availableServers.addAll(serverInfoList);
        }
    }

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
                        ClientProxyNotifier.getInstance().notifyServerAvailable(serverInfo);
                    }
                }
            }
        }, delayMS, intervalMS);
    }

    public T getClient() {
        int index = this.loadBalance.getIndex(null);
        return (T) getClient(index);
    }

    public T getClient(String key) {
        int index = this.loadBalance.getIndex(key);
        return (T) getClient(index);
    }

    protected ClosableClient getClient(int index){
        if(this.availableServers.isEmpty()){
            logger.error("available server list is empty");
            return null;
        }
        ServerInfo serverInfo = availableServers.get(index);
        ClosableClient client = serverInfo.getClient();
        if(client == null){
            client = createClient(serverInfo);
        }
        return client;
    }

    protected ClosableClient createClient(ServerInfo serverInfo){
        ClosableClient client = clientProxyFactory.getClientProxy(serverInfo);
        serverInfo.setClient(client);
        return client;
    }

    protected void destroyClient(ServerInfo serverInfo){
        ClosableClient client = serverInfo.getClient();
        client.close();
        serverInfo.setClient(null);
    }


    protected void toAvailable(ServerInfo serverInfo) {
        if(this.unavailableServers.contains(serverInfo)){
            createClient(serverInfo);
            this.availableServers.add(serverInfo);
            this.unavailableServers.remove(serverInfo);
            this.initLoadBalance();
        }
    }

    protected void toUnavailable(ServerInfo serverInfo) {
        if(this.availableServers.contains(serverInfo)) {
            this.destroyClient(serverInfo);
            this.availableServers.remove(serverInfo);
            this.unavailableServers.add(serverInfo);
            this.initLoadBalance();
        }
    }


}
