package com.eproxy;

import com.eproxy.exception.DefaultExceptionHandler;
import com.eproxy.exception.DefaultSwitchPolicy;
import com.eproxy.exception.ExceptionHandler;
import com.eproxy.exception.SwitchPolicy;
import com.eproxy.loadbalance.LoadBalanceStrategy;

/**
 * 服务代理的配置信息
 * @author 谢俊权
 * @create 2016/9/5 14:26
 */
public class Configure {


    /**
     * 定时检查服务是否可用的间隔时间
     */
    private long checkServerAvailableIntervalMs = 1000 * 60 * 10;

    /**
     * 客户端调用报错间的平均最小时间, 如果小于这个时间则设置此客户端不可用
     */
    private int minExceptionFrequency = 1;

    /**
     * 客户端调用报错的最大次数, 如果大于这个次数则设置此客户端不可用
     */
    private int maxExceptionTimes = 2;

    /**
     * 负载均衡的策略
     */
    private LoadBalanceStrategy loadBalanceStrategy = LoadBalanceStrategy.HASH;

    /**
     * 客户端方法调用报错的处理器
     */
    private ExceptionHandler exceptionHandler;

    private SwitchPolicy switchPolicy;


    public Configure(Builder builder) {
        this.checkServerAvailableIntervalMs = builder.checkServerAvailableIntervalMs;
        this.loadBalanceStrategy = builder.loadBalanceStrategy;
        this.exceptionHandler = builder.exceptionHandler;
        this.switchPolicy = builder.switchPolicy;
        if(this.exceptionHandler == null){
           this.exceptionHandler = new DefaultExceptionHandler();
        }
        if(this.switchPolicy == null){
            this.switchPolicy = new DefaultSwitchPolicy(minExceptionFrequency, maxExceptionTimes);
        }

    }

    public long getCheckServerAvailableIntervalMs() {
        return checkServerAvailableIntervalMs;
    }

    public LoadBalanceStrategy getLoadBalanceStrategy() {
        return loadBalanceStrategy;
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public SwitchPolicy getSwitchPolicy() {
        return switchPolicy;
    }

    public static class Builder{

        private long checkServerAvailableIntervalMs = 1000 * 60 * 10;

        private LoadBalanceStrategy loadBalanceStrategy = LoadBalanceStrategy.HASH;

        private ExceptionHandler exceptionHandler;

        private SwitchPolicy switchPolicy;

        /**
         * 设置定时检查服务是否可用的间隔时间
         * @param checkServerAvailableIntervalMs
         * @return
         */
        public Builder checkServerAvailableIntervalMs(long checkServerAvailableIntervalMs){
            this.checkServerAvailableIntervalMs = checkServerAvailableIntervalMs;
            return this;
        }

        /**
         * 设置负载均衡的策略
         * @param loadBalanceStrategy
         * @return
         */
        public Builder loadBalanceStrategy(LoadBalanceStrategy loadBalanceStrategy){
            this.loadBalanceStrategy = loadBalanceStrategy;
            return this;
        }


        /**
         * 调用客户端方法报错之后的处理器
         * @param exceptionHandler
         * @return
         */
        public Builder exceptionHandler(ExceptionHandler exceptionHandler){
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        /**
         * 在报错之后判断是否需要切换服务的策略
         * @param switchPolicy
         * @return
         */
        public Builder switchPolicy(SwitchPolicy switchPolicy){
            this.switchPolicy = switchPolicy;
            return this;
        }

        public Configure build(){
            return new Configure(this);
        }

    }



}
