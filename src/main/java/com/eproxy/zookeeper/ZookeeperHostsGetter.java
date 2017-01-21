package com.eproxy.zookeeper;

/**
 * @author xiejunquan
 * @create 2017/1/17 11:15
 */
public interface ZookeeperHostsGetter {

    String get(long groupId);
}
