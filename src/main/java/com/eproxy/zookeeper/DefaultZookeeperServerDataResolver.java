package com.eproxy.zookeeper;

import com.eproxy.ServerConfigureResolver;
import com.eproxy.ServerInfo;

import java.util.List;
import java.util.Map;

/**
 * @author xiejunquan
 * @create 2017/1/13 15:06
 */
public class DefaultZookeeperServerDataResolver implements ZookeeperServerDataResolver{

    @Override
    public List<ServerInfo> get(byte[] data) {
        String hosts = new String(data);
        return ServerConfigureResolver.getServerInfoList(hosts, null);
    }
}
