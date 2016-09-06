package com.bj.proxy;

import com.bj.loadbalance.LoadBalanceStrategy;

/**
 * @author 谢俊权
 * @create 2016/9/5 14:26
 */
public class Configure {


    private long checkServerAvailableIntervalMs = 1000 * 60 * 10;

    private long telnetTimeoutMs = 1000 * 5;

    private long minExceptionFrequencyMs = 1000 * 2;

    private int maxExceptionTimes = 10;

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

        public Builder checkServerAvailableIntervalMs(long checkServerAvailableIntervalMs){
            this.checkServerAvailableIntervalMs = checkServerAvailableIntervalMs;
            return this;
        }

        public Builder telnetTimeoutMs(long telnetTimeoutMs){
            this.telnetTimeoutMs = telnetTimeoutMs;
            return this;
        }

        public Builder minExceptionFrequencyMs(long minExceptionFrequencyMs){
            this.minExceptionFrequencyMs = minExceptionFrequencyMs;
            return this;
        }

        public Builder maxExceptionTimes(int maxExceptionTimes){
            this.maxExceptionTimes = maxExceptionTimes;
            return this;
        }

        public Builder loadBalanceStrategy(LoadBalanceStrategy loadBalanceStrategy){
            this.loadBalanceStrategy = loadBalanceStrategy;
            return this;
        }

        public Configure build(){
            return new Configure(this);
        }

    }



}
