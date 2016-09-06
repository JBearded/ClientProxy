package com.bj.loadbalance;

/**
 * @author 谢俊权
 * @create 2016/5/6 10:37
 */
public interface LoadBalance {

    int getIndex(String key);
}
