package com.eproxy.zookeeper;

import com.eproxy.EasyProxyNotifier;
import com.eproxy.ProxyConfigure;
import com.eproxy.ServerConfigure;
import com.eproxy.ServerInfo;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 谢俊权
 * @create 2016/6/2 10:24
 */
public class ZookeeperClient {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperClient.class);

    private static ZooKeeper zooKeeper;
    private static String proxyNodePath;

    private EasyProxyNotifier notifier;
    private ProxyConfigure proxyConfigure;
    private ServerConfigure serverConfigure;


    public ZookeeperClient(EasyProxyNotifier notifier, ProxyConfigure proxyConfig, ServerConfigure serverConfig) {
        this.notifier = notifier;
        this.proxyConfigure = proxyConfig;
        this.serverConfigure = serverConfig;
        this.proxyNodePath = getProxyNodePath();
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
                        zooKeeper.getChildren(proxyNodePath, this);  //重新注册监听
                        String eventPath = watchedEvent.getPath();
                        if (eventPath != null && eventPath.startsWith(proxyNodePath)) {
                            switch (watchedEvent.getType()){
                                case None:
                                    break;
                                case NodeCreated:
                                    break;
                                case NodeDeleted:
                                    break;
                                case NodeDataChanged:
                                    break;
                                case NodeChildrenChanged:
                                    List<String> nodeList = zooKeeper.getChildren(proxyNodePath, true);
                                    syncServerData(nodeList);
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        logger.error("error to get changed zookeeper node data and register watcher", e);
                    }
                }
            };
            zooKeeper.getChildren(proxyNodePath, watcher, new AsyncCallback.ChildrenCallback() {
                @Override
                public void processResult(int rc, String path, Object ctx, List<String> children) {
                    syncServerData(children);
                }
            }, null);

        } catch (Exception e) {
            logger.error("error to get zookeeper groupIdL{} server:{}", serverConfigure.getGroupId(), serverConfigure.getServerName(), e);
        }
    }

    private void syncServerData(List<String> nodeList){
        List<ServerInfo> list = new ArrayList<>();
        for(String node : nodeList){
            ZookeeperServerDataResolver resolver = proxyConfigure.getZookeeperServerDataResolver();
            ServerInfo serverInfo = resolver.get(node);
            serverInfo.setExtendInfoMap(serverConfigure.getExtendInfo());
            list.add(serverInfo);
        }
        notifier.updateAvailableServers(list);
    }

    private String getProxyNodePath(){
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
