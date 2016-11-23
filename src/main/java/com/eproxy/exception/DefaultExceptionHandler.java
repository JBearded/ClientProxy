package com.eproxy.exception;

import com.eproxy.ServerInfo;
import com.eproxy.EasyProxyNotifier;
import com.eproxy.Configure;
import com.eproxy.utils.TelnetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xiejunquan
 * @create 2016/11/23 14:51
 */
public class DefaultExceptionHandler implements ExceptionHandler{

    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    private Configure configure;
    private ExceptionPolicy policy;

    public DefaultExceptionHandler() {
        this(new Configure.Builder().build());
    }

    public DefaultExceptionHandler(Configure configure) {
        this.configure = (configure == null) ? new Configure.Builder().build() : configure;
        long minExceptionFrequencyMs = configure.getMinExceptionFrequencyMs();
        int maxExceptionTimes = configure.getMaxExceptionTimes();
        this.policy = new ExceptionPolicy(minExceptionFrequencyMs, maxExceptionTimes);
    }

    @Override
    public void handle(MethodExceptionInfo exceptionInfo) {
        Throwable e = exceptionInfo.getThrowable();
        ServerInfo serverInfo = exceptionInfo.getServerInfo();

        if(!(e.getCause() instanceof IgnoredException)){

            if(!TelnetUtil.isConnect(serverInfo.getIp(), serverInfo.getPort(), configure.getTelnetTimeoutMs())){
                if(policy.needChangeClient(serverInfo.getIp(), serverInfo.getPort())){
                    EasyProxyNotifier.getInstance().notifyServerUnavailable(serverInfo);
                }
            }
        }
    }
}
