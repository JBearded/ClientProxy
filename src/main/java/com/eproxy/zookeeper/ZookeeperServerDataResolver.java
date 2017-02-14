package com.eproxy.zookeeper;

import com.eproxy.ServerInfo;

/**
 * @author xiejunquan
 * @create 2017/1/12 15:37
 */
public interface ZookeeperServerDataResolver {

    ServerInfo get(String data);
}
