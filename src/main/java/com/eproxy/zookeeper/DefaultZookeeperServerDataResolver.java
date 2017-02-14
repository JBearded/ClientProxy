package com.eproxy.zookeeper;

import com.eproxy.ServerConfigureResolver;
import com.eproxy.ServerInfo;

import java.util.List;

/**
 * @author xiejunquan
 * @create 2017/1/13 15:06
 */
public class DefaultZookeeperServerDataResolver implements ZookeeperServerDataResolver{

    @Override
    public ServerInfo get(String data) {
        String hosts = new String(data);
        List<ServerInfo> list = ServerConfigureResolver.getServerInfoList(hosts, null);
        return (list.isEmpty()) ? null : list.get(0);
    }
}
