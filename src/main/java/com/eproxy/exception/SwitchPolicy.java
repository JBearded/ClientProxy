package com.eproxy.exception;

/**
 * @author 谢俊权
 * @create 2016/12/31 11:53
 */
public interface SwitchPolicy {

    boolean needSwitch(String ip, int port);
}
