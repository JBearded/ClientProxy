package com.eproxy.zookeeper;

import com.eproxy.EasyProxyNotifier;
import com.eproxy.ProxyConfigure;
import com.eproxy.ServerConfigure;
import com.eproxy.ServerInfo;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author 谢俊权
 * @create 2016/6/2 10:24
 */
public class ZookeeperClient {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperClient.class);

    private static ZooKeeper zooKeeper = null;
    private static String proxyNodePath = null;

    private ProxyConfigure proxyConfigure;

    private ServerConfigure serverConfigure;


    public ZookeeperClient(final ProxyConfigure proxyConfig, ServerConfigure serverConfig) {
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
                        String eventPath = watchedEvent.getPath();
                        if (eventPath.startsWith(proxyNodePath)) {
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
                                    List<String> nodeList = zooKeeper.getChildren(proxyNodePath, false);
                                    syncServerData(nodeList);
                                    break;
                            }
                        }
                        zooKeeper.exists(proxyNodePath, this);  //重新注册监听
                    } catch (Exception e) {
                        logger.error("error to get changed zookeeper node data and register watcher");
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
        for (String node : nodeList) {
            String serverNode = proxyNodePath + "/" + node;
            byte[] data = new byte[0];
            try {
                data = zooKeeper.getData(serverNode, false, null);
            } catch (Exception e) {
                logger.error("error get zookeeper node data, node:{}", serverNode);
            }
            syncServerData(data);
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
