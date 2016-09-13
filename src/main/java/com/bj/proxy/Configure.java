package com.bj.proxy;

import com.bj.loadbalance.LoadBalanceStrategy;

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
     * 检查服务是否可用的timeout时间
     */
    private long telnetTimeoutMs = 1000 * 5;

    /**
     * 客户端调用报错间的平均最小时间, 如果小于这个时间则设置此客户端不可用
     */
    private long minExceptionFrequencyMs = 1000 * 2;

    /**
     * 客户端调用报错的最大次数, 如果大于这个次数则设置此客户端不可用
     */
    private int maxExceptionTimes = 10;

    /**
     * 负载均衡的策略
     */
    private LoadBalanceStrategy loadBalanceStrategy = LoadBalanceStrategy.HASH;

    public Configure(Builder builder) {
        this.checkServerAvailableIntervalMs = builder.checkServerAvailableIntervalMs;
        this.telnetTimeoutMs = builder.telnetTimeoutMs;
        this.minExceptionFrequencyMs = builder.minExceptionFrequencyMs;
        this.maxExceptionTimes = builder.maxExceptionTimes;
        this.loadBalanceStrategy = builder.loadBalanceStrategy;
    }

    public long getCheckServerAvailableIntervalMs() {
        return checkServerAvailableIntervalMs;
    }

    public long getTelnetTimeoutMs() {
        return telnetTimeoutMs;
    }

    public long getMinExceptionFrequencyMs() {
        return minExceptionFrequencyMs;
    }

    public int getMaxExceptionTimes() {
        return maxExceptionTimes;
    }

    public LoadBalanceStrategy getLoadBalanceStrategy() {
        return loadBalanceStrategy;
    }

    public static class Builder{

        private long checkServerAvailableIntervalMs = 1000 * 60 * 10;

        private long telnetTimeoutMs = 1000 * 5;

        private long minExceptionFrequencyMs = 1000 * 2;

        private int maxExceptionTimes = 10;

        private LoadBalanceStrategy loadBalanceStrategy = LoadBalanceStrategy.HASH;

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
         * 设置检查服务是否可用的timeout时间
         * @param telnetTimeoutMs
         * @return
         */
        public Builder telnetTimeoutMs(long telnetTimeoutMs){
            this.telnetTimeoutMs = telnetTimeoutMs;
            return this;
        }

        /**
         * 设置客户端调用报错间的平均最小时间, 如果小于这个时间则设置此客户端不可用
         * @param minExceptionFrequencyMs
         * @return
         */
        public Builder minExceptionFrequencyMs(long minExceptionFrequencyMs){
            this.minExceptionFrequencyMs = minExceptionFrequencyMs;
            return this;
        }

        /**
         * 设置客户端调用报错的最大次数, 如果大于这个次数则设置此客户端不可用
         * @param maxExceptionTimes
         * @return
         */
        public Builder maxExceptionTimes(int maxExceptionTimes){
            this.maxExceptionTimes = maxExceptionTimes;
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

        public Configure build(){
            return new Configure(this);
        }

    }



}
