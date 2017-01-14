package com.eproxy.zookeeper;

import com.eproxy.ServerInfo;

import java.util.List;

/**
 * @author xiejunquan
 * @create 2017/1/12 15:37
 */
public interface ZookeeperServerDataResolver {

    List<ServerInfo> get(byte[] data);
}
