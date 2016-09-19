package com.client.proxy;

import com.client.exception.IgnoredException;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 动态代理的处理类
 * @author 谢俊权
 * @create 2016/5/6 17:42
 */
public class ClientHandler implements MethodInterceptor{

    private static final Logger logger  = LoggerFactory.getLogger(ClientHandler.class);

    private ClientExceptionPolicy clientExceptionPolicy;
    private Configure configure;

    private ClientInfo clientInfo;

    public ClientHandler(ClientInfo clientInfo, Configure configure) {
        this.clientInfo = clientInfo;
        this.configure = configure;
        this.clientExceptionPolicy = new ClientExceptionPolicy(configure.getMinExceptionFrequencyMs(), configure.getMaxExceptionTimes());
    }

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        Object result = null;
        try{
            result = methodProxy.invokeSuper(object, args);
        }catch (Throwable e){

            if(!(e.getCause() instanceof IgnoredException)){

                if(!TelnetUtil.isConnect(clientInfo.getIp(), clientInfo.getPort(), configure.getTelnetTimeoutMs())){
                    if(clientExceptionPolicy.needChangeClient(clientInfo.getIp(), clientInfo.getPort())){
                        ClientProxyNotifier.getInstance().notifyServerUnavailable(clientInfo);
                    }
                }
            }
            logger.error("error call method {}", method.getName(), e);
        }
        return result;
    }

}
