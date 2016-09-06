package com.bj.proxy;

import java.util.List;

/**
 * @author 谢俊权
 * @create 2016/9/2 15:59
 */
public abstract class ServerInfoResolver {

    protected String configPath;

    public ServerInfoResolver(String configPath) {
        this.configPath = configPath;
    }

    public abstract List<ServerInfo> get();
}
