package com.eproxy.exception;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 谢俊权
 * @create 2016/8/31 17:57
 */
public class DefaultSwitchPolicy implements SwitchPolicy{

    private int maxCountExceptionSecondTime = 60;

    private int maxExceptionTimes = 20;

    private ConcurrentMap<String, ExceptionInfo> exceptionInfoMap = new ConcurrentHashMap<>();

    public DefaultSwitchPolicy(int maxCountExceptionSecondTime, int maxExceptionTimes) {
        this.maxCountExceptionSecondTime = maxCountExceptionSecondTime;
        this.maxExceptionTimes = maxExceptionTimes;
    }

    /**
     * 是否需要改变客户端(此服务的客户端已不可用)
     * @param ip    ip
     * @param port  端口
     * @return
     */
    @Override
    public synchronized boolean needSwitch(String ip, int port){
        recordException(ip, port);
        String key = getKey(ip, port);
        boolean need = false;
        if(exceptionInfoMap.containsKey(key)){
            ExceptionInfo exceptionInfo = exceptionInfoMap.get(key);
            long fistTime = exceptionInfo.firstExceptionTime;
            long currTime = exceptionInfo.currentExceptionTime;
            int times = exceptionInfo.exceptionTimes;
            long timeLength = (currTime - fistTime) / 1000L;
            if(timeLength >= maxCountExceptionSecondTime || times >= maxExceptionTimes) {
                need = true;
            }
        }
        if(need){
            exceptionInfoMap.remove(key);
        }
        return need;
    }

    /**
     * 记录报错信息
     * @param ip ip
     * @param port  端口
     */
    private void recordException(String ip, int port){
        String key = getKey(ip, port);
        ExceptionInfo exceptionInfo = exceptionInfoMap.get(key);
        if(exceptionInfo == null){
            exceptionInfo = new ExceptionInfo();
            exceptionInfoMap.put(key, exceptionInfo);
        }
        exceptionInfo.increaseTimes();
    }

    private String getKey(String ip, int port){
        return ip + ":" + port;
    }


    class ExceptionInfo{
        private int exceptionTimes = 0;
        private long firstExceptionTime = 0;
        private long currentExceptionTime = 0;

        public ExceptionInfo() {
            this.firstExceptionTime = System.currentTimeMillis();
            this.currentExceptionTime = firstExceptionTime;
        }

        private void increaseTimes(){
            exceptionTimes++;
            currentExceptionTime = System.currentTimeMillis();
        }

    }


}
