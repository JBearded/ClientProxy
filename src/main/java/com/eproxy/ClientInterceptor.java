package com.eproxy;

import com.eproxy.exception.ExceptionHandler;
import com.eproxy.exception.IgnoredException;
import com.eproxy.exception.MethodExceptionInfo;
import com.eproxy.exception.SwitchPolicy;
import com.eproxy.utils.TelnetUtil;
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
public class ClientInterceptor implements MethodInterceptor{

    private static final Logger logger  = LoggerFactory.getLogger(ClientInterceptor.class);

    private ClosableClient client;
    private Configure configure;
    private ServerInfo serverInfo;

    public ClientInterceptor(ClosableClient client, ServerInfo serverInfo, Configure configure) {
        this.client = client;
        this.serverInfo = serverInfo;
        this.configure = configure;
    }

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        Object result = null;
        try{
            result = method.invoke(client, args);
        }catch (Throwable e){

            if(!(e.getCause() instanceof IgnoredException)){
                if(!TelnetUtil.isConnect(serverInfo.getIp(), serverInfo.getPort())){
                    SwitchPolicy policy = configure.getSwitchPolicy();
                    if(policy.needSwitch(serverInfo.getIp(), serverInfo.getPort())){
                        EasyProxyNotifier.getInstance().notifyServerUnavailable(serverInfo);
                    }
                }
            }
            MethodExceptionInfo exceptionInfo = new MethodExceptionInfo(e, serverInfo, object, method, args);
            ExceptionHandler handler = configure.getExceptionHandler();
            handler.handle(exceptionInfo);
        }
        return result;
    }

}
