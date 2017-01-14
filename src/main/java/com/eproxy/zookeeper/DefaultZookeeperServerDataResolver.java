package com.eproxy.zookeeper;

import com.eproxy.ServerInfo;

import java.util.List;

/**
 * @author xiejunquan
 * @create 2017/1/13 15:06
 */
public class DefaultZookeeperServerDataResolver implements ZookeeperServerDataResolver{

    @Override
    public List<ServerInfo> get(byte[] data) {
        return null;
    }
}
