package com.client.proxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 客户端类的构造信息
 * @author 谢俊权
 * @create 2016/9/2 15:04
 */
public class ClientConstructor {

    private Class<? extends ClosableClient> clientClass;
    private List<Object> parameters = new ArrayList<Object>();

    public ClientConstructor(Class<? extends ClosableClient> clientClass, Object... parameters) {
        this.clientClass = clientClass;
        this.parameters.addAll(Arrays.asList(parameters));
    }

    public Class<? extends ClosableClient> getClientClass() {
        return clientClass;
    }

    public List<Object> getParameters() {
        return parameters;
    }
}
