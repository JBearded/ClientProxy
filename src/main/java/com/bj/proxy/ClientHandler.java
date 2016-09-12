package com.bj.proxy;

import com.bj.exception.IgnoredException;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 动态代理
 * @author 谢俊权
 * @create 2016/5/6 17:42
 */
public class ClientHandler implements MethodInterceptor{

    private static final Logger logger  = LoggerFactory.getLogger(ClientHandler.class);

    private ReLoadBalancePolicy accessPolicy;
    private Configure configure;

    private ServerInfo serverInfo;

    public ClientHandler(ServerInfo serverInfo, Configure configure) {
        this.serverInfo = serverInfo;
        this.configure = configure;
        this.accessPolicy = new ReLoadBalancePolicy(configure.getMinExceptionFrequencyMs(), configure.getMaxExceptionTimes());
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
                        ClientProxyNotifier.getInstance().notifyServerUnavailable(serverInfo);
                    }
                }
            }
            logger.error("error call method {}", method.getName(), e);
        }
        return result;
    }

}
