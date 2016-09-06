package com.bj.proxy;

import net.sf.cglib.proxy.Enhancer;

import java.util.List;

/**
 * @author 谢俊权
 * @create 2016/5/6 17:46
 */
public class ClientProxyFactory {

    private Configure configure;

    public ClientProxyFactory(Configure configure) {
        this.configure = configure;
    }

    public ClosableClient getClientProxy(ServerInfo serverInfo){

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(serverInfo.getClientConstructor().getClientClass());
        ClientProxyHandler handler = new ClientProxyHandler(serverInfo, configure);
        enhancer.setCallback(handler);
        ClosableClient result = null;
        if(!serverInfo.getClientConstructor().getParameters().isEmpty()){
            Class<?>[] types = getConstructorTypes(serverInfo.getClientConstructor());
            Object[] values = getConstructorValues(serverInfo.getClientConstructor());
            result = (ClosableClient) enhancer.create(types, values);
        }else{
            result = (ClosableClient) enhancer.create();
        }
        return result;
    }

    private Class[] getConstructorTypes(ClientConstructor clientConstructor){
        List<ClientConstructor.Parameter> parameterList = clientConstructor.getParameters();
        Class[] types = new Class[parameterList.size()];
        for (int i = 0; i < parameterList.size(); i++) {
            types[i] = parameterList.get(i).getType();
        }
        return types;
    }

    private Object[] getConstructorValues(ClientConstructor clientConstructor){
        List<ClientConstructor.Parameter> parameterList = clientConstructor.getParameters();
        Object[] values = new Object[parameterList.size()];
        for (int i = 0; i < parameterList.size(); i++) {
            values[i] = parameterList.get(i).getValue();
        }
        return values;
    }

}
