package com.eproxy.zookeeper;

import com.eproxy.EasyProxyNotifier;
import com.eproxy.ProxyConfigure;
import com.eproxy.ServerConfigure;
import com.eproxy.ServerInfo;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.prefs.NodeChangeEvent;

/**
 * @author 谢俊权
 * @create 2016/6/2 10:24
 */
public class ZookeeperClient {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperClient.class);

    private static ZooKeeper zooKeeper = null;
    private static String nodePath = null;

    private ProxyConfigure proxyConfigure;

    private ServerConfigure serverConfigure;


    public ZookeeperClient(final ProxyConfigure proxyConfig, ServerConfigure serverConfig) {
        this.proxyConfigure = proxyConfig;
        this.serverConfigure = serverConfig;
        this.nodePath = getNodePath();
        long groupId = serverConfigure.getGroupId();
        ZookeeperHostsGetter zookeeperHostsGetter = proxyConfigure.getZookeeperHostsGetter();
        String hosts = (zookeeperHostsGetter != null) ? zookeeperHostsGetter.get(groupId) : serverConfig.getZookeeperHosts();
        try {
            zooKeeper = new ZooKeeper(hosts, 5000, null);
        } catch (Exception e) {
            logger.error("error to new zookeeper, hosts:{}", hosts, e);
        }
    }

    public void asyncServerData(){
        try {
            Watcher watcher = new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    try {
                        String eventPath = watchedEvent.getPath();
                        if (nodePath.equals(eventPath)) {
                            switch (watchedEvent.getType()){
                                case None:
                                    break;
                                case NodeCreated:
                                    break;
                                case NodeDeleted:
                                    break;
                                case NodeDataChanged:
                                    byte[] data = zooKeeper.getData(nodePath, false, null);
                                    syncServerData(data);
                                    break;
                                case NodeChildrenChanged:
                                    break;
                            }
                        }
                        zooKeeper.exists(nodePath, this);
                    } catch (Exception e) {
                        logger.error("error to get changed zookeeper node data and register watcher");
                    }
                }
            };
            zooKeeper.getData(nodePath, watcher, new AsyncCallback.DataCallback() {
                @Override
                public void processResult(int i, String s, Object o, byte[] bytes, Stat stat) {
                    syncServerData(bytes);
                }
            }, null);
        } catch (Exception e) {
            logger.error("error to get zookeeper groupIdL{} server:{}", serverConfigure.getGroupId(), serverConfigure.getServerName(), e);
        }
    }

    private void syncServerData(byte[] bytes){
        if(bytes != null || bytes.length > 0){
            EasyProxyNotifier notifier = EasyProxyNotifier.getInstance();
            ZookeeperServerDataResolver resolver = proxyConfigure.getZookeeperServerDataResolver();
            List<ServerInfo> list = resolver.get(bytes);
            for (ServerInfo serverInfo : list) {
                System.out.println(serverInfo);
                notifier.notifyServerAvailable(serverInfo);
            }
        }
    }

    private String getNodePath(){
        long groupId = serverConfigure.getGroupId();
        String serverName = serverConfigure.getServerName();
        return new StringBuilder("/")
                .append("easy").append("/")
                .append("proxy").append("/")
                .append(groupId).append("/")
                .append(serverName)
                .toString();
    }
}
