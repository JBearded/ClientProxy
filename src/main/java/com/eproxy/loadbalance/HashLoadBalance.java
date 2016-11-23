package com.eproxy.loadbalance;

/**
 * 哈希调度
 *
 * @author 谢俊权
 * @create 2016/5/6 16:51
 */
public class HashLoadBalance implements LoadBalance{

    private int serverSize;

    public HashLoadBalance(int serverSize) {
        this.serverSize = serverSize;
    }

    @Override
    public int getIndex(String key) {
        String hashKey = (key == null) ? String.valueOf(0) : key;
        int hash = Math.abs(hashKey.hashCode());
        return hash % this.serverSize;
    }
}
