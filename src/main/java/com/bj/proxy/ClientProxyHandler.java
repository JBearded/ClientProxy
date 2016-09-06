package com.bj.proxy;

import com.bj.exception.IgnoredException;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * 动态代理
 * @author 谢俊权
 * @create 2016/5/6 17:42
 */
public class ClientProxyHandler implements MethodInterceptor{

    private ReLoadBalancePolicy accessPolicy;
    private Configure configure;

    private ClientProxyNotifier notifier;
    private ServerInfo serverInfo;

    public ClientProxyHandler(ServerInfo serverInfo, Configure configure) {
        this.serverInfo = serverInfo;
        this.configure = configure;
        this.accessPolicy = new ReLoadBalancePolicy(configure.getMinExceptionFrequencyMs(), configure.getMaxExceptionTimes());
        this.notifier = ClientProxyNotifier.getInstance();
    }

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        Object result = null;
        try{
            result = methodProxy.invokeSuper(object, args);
        }catch (Throwable e){

            if(!(e.getCause() instanceof IgnoredException)){

                if(!TelnetUtil.isConnect(serverInfo.getIp(), serverInfo.getPort(), configure.getTelnetTimeoutMs())){
                    accessPolicy.hitException(serverInfo.getIp(), serverInfo.getPort());
                    if(accessPolicy.needChangeClient(serverInfo.getIp(), serverInfo.getPort())){
                        notifier.notifyServerUnavailable(serverInfo);
                    }
                }
            }
            throw new RuntimeException(e);
        }
        return result;
    }

}
