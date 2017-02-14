package com.eproxy;

import com.eproxy.exception.ExceptionHandler;
import com.eproxy.exception.IgnoredException;
import com.eproxy.exception.MethodExceptionInfo;
import com.eproxy.exception.SwitchPolicy;
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
    private EasyProxyNotifier notifier;
    private ProxyConfigure proxyConfigure;
    private ServerInfo serverInfo;

    public ClientInterceptor(ClosableClient client, EasyProxyNotifier notifier, ServerInfo serverInfo, ProxyConfigure proxyConfigure) {
        this.client = client;
        this.notifier = notifier;
        this.serverInfo = serverInfo;
        this.proxyConfigure = proxyConfigure;
    }

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        Object result = null;
        try{
            result = method.invoke(client, args);
        }catch (Throwable e){

            logger.error("error to call method:{}", method.getName(), e);

            if(!(e.getCause() instanceof IgnoredException)){
                // 此处只做策略切换, 而不使用telnet检测服务可用, 避免大量失败时出现大量线程堵塞
                SwitchPolicy policy = proxyConfigure.getSwitchPolicy();
                if(policy.needSwitch(serverInfo.getIp(), serverInfo.getPort())){
                    notifier.serverUnavailable(serverInfo);
                }
            }
            ServerInfo newServerInfo = new ServerInfo(serverInfo.getIp(), serverInfo.getPort(), serverInfo.getWeight());
            MethodExceptionInfo exceptionInfo = new MethodExceptionInfo(e, newServerInfo, object, method, args);
            ExceptionHandler handler = proxyConfigure.getExceptionHandler();
            handler.handle(exceptionInfo);
        }
        return result;
    }

}
