package com.eproxy;

import com.eproxy.exception.ExceptionHandler;
import com.eproxy.exception.MethodExceptionInfo;
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

    private Configure configure;
    private ServerInfo serverInfo;

    public ClientHandler(ServerInfo serverInfo, Configure configure) {
        this.serverInfo = serverInfo;
        this.configure = configure;
    }

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        Object result = null;
        try{
            result = methodProxy.invokeSuper(object, args);
        }catch (Throwable e){
            MethodExceptionInfo exceptionInfo = new MethodExceptionInfo(e, serverInfo, object, method, args);
            ExceptionHandler handler = configure.getExceptionHandler();
            handler.handle(exceptionInfo);
        }
        return result;
    }

}
