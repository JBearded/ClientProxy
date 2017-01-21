package com.eproxy.zookeeper;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author 谢俊权
 * @create 2016/6/2 10:24
 */
public class ZookeeperClient {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperClient.class);

    private static ZooKeeper zooKeeper = null;
    private static String ROOT = "/easy/proxy";
    private static long groupId = 0L;


    public ZookeeperClient(String hosts) {
        try {
            zooKeeper = new ZooKeeper(hosts, 5000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {

                }
            });
        } catch (IOException e) {
            logger.error("error to new zookeeper, hosts:{}", hosts, e);
        }
    }

    public void asynchServerData(String serverName, AsyncCallback.DataCallback dataCallback){
        try {
            String node = new StringBuilder()
                    .append(ROOT).append("/")
                    .append(groupId).append("/")
                    .append(serverName)
                    .toString();
            zooKeeper.getData(node, true, dataCallback, null);
        } catch (Exception e) {
            logger.error("error to get zookeeper server:{}", serverName, e);
        }
    }
}
