package com.eproxy.utils;

import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * telnet工具
 * @author 谢俊权
 * @create 2016/9/2 11:48
 */
public class TelnetUtil {

    private static final Logger logger = LoggerFactory.getLogger(TelnetUtil.class);

    /**
     * ip:port 是否能够连通
     * @param ip    ip
     * @param port  端口
     * @param timeoutMS 连接过时时间
     * @return
     */
    public static boolean isConnect(String ip, int port, long timeoutMS){
        boolean isConnect = false;
        int defaultTimeout = 1000 * 10;
        int timeout = (int) timeoutMS;
        TelnetClient telnetClient = new TelnetClient();
        telnetClient.setDefaultTimeout((timeout <= 0) ? defaultTimeout : timeout);
        try {
            telnetClient.connect(ip, port);
            isConnect = telnetClient.isConnected();
        } catch (IOException e) {
            logger.error("error connect ip:{} port:{} timeout:{}", ip, port, timeoutMS, e);
        }finally {
            try {
                telnetClient.disconnect();
            } catch (IOException e) {
                logger.error("error disconnect ip:{} port:{}", ip, port, e);
            }
        }
        return isConnect;
    }
}
