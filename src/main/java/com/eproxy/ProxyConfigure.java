package com.eproxy;

import com.eproxy.exception.DefaultExceptionHandler;
import com.eproxy.exception.DefaultSwitchPolicy;
import com.eproxy.exception.ExceptionHandler;
import com.eproxy.exception.SwitchPolicy;
import com.eproxy.loadbalance.LoadBalanceStrategy;
import com.eproxy.zookeeper.DefaultZookeeperServerDataResolver;
import com.eproxy.zookeeper.ZookeeperHostsGetter;
import com.eproxy.zookeeper.ZookeeperServerDataResolver;

/**
 * 服务代理的配置信息
 * @author 谢俊权
 * @create 2016/9/5 14:26
 */
public class ProxyConfigure {


    /**
     * 定时检查服务是否可用的间隔时间
     */
    private long checkServerAvailableIntervalMs = 1000 * 60 * 10;

    /**
     * telnet检查服务是否可用的超时时间
     */
    private int telnetTimeoutMs = 1000 * 2;

    /**
     * 统计客户端调用报错次数的最大时间
     */
    private int maxCountExceptionSecondTime = 60;

    /**
     * 客户端调用报错的最大次数, 如果大于这个次数则设置此客户端不可用
     */
    private int maxExceptionTimes = 3;

    /**
     * 负载均衡的策略
     */
    private LoadBalanceStrategy loadBalanceStrategy = LoadBalanceStrategy.HASH;

    /**
     * 客户端方法调用报错的处理器
     */
    private ExceptionHandler exceptionHandler;

    private SwitchPolicy switchPolicy;

    private ZookeeperServerDataResolver zookeeperServerDataResolver;

    private ZookeeperHostsGetter zookeeperHostsGetter;


    public ProxyConfigure(Builder builder) {
        this.checkServerAvailableIntervalMs = builder.checkServerAvailableIntervalMs;
        this.telnetTimeoutMs = builder.telnetTimeoutMs;
        this.loadBalanceStrategy = builder.loadBalanceStrategy;
        this.exceptionHandler = builder.exceptionHandler;
        this.switchPolicy = builder.switchPolicy;
        this.zookeeperServerDataResolver = builder.zookeeperServerDataResolver;
        this.zookeeperHostsGetter = builder.zookeeperHostsGetter;
        if(this.exceptionHandler == null){
           this.exceptionHandler = new DefaultExceptionHandler();
        }
        if(this.switchPolicy == null){
            this.switchPolicy = new DefaultSwitchPolicy(maxCountExceptionSecondTime, maxExceptionTimes);
        }
        if(zookeeperServerDataResolver == null){
            this.zookeeperServerDataResolver = new DefaultZookeeperServerDataResolver();
        }

    }

    public long getCheckServerAvailableIntervalMs() {
        return checkServerAvailableIntervalMs;
    }

    public int getTelnetTimeoutMs() {
        return telnetTimeoutMs;
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

    public ZookeeperServerDataResolver getZookeeperServerDataResolver() {
        return zookeeperServerDataResolver;
    }

    public ZookeeperHostsGetter getZookeeperHostsGetter() {
        return zookeeperHostsGetter;
    }

    public static class Builder{

        private long checkServerAvailableIntervalMs = 1000 * 60 * 10;

        private int telnetTimeoutMs = 1000 * 2;

        private LoadBalanceStrategy loadBalanceStrategy = LoadBalanceStrategy.HASH;

        private ExceptionHandler exceptionHandler;

        private SwitchPolicy switchPolicy;

        private ZookeeperServerDataResolver zookeeperServerDataResolver;

        private ZookeeperHostsGetter zookeeperHostsGetter;

        public Builder checkServerAvailableIntervalMs(long checkServerAvailableIntervalMs){
            this.checkServerAvailableIntervalMs = checkServerAvailableIntervalMs;
            return this;
        }

        public Builder telnetTimeoutMs(int telnetTimeoutMs){
            this.telnetTimeoutMs = telnetTimeoutMs;
            return this;
        }

        public Builder loadBalanceStrategy(LoadBalanceStrategy loadBalanceStrategy){
            this.loadBalanceStrategy = loadBalanceStrategy;
            return this;
        }

        public Builder exceptionHandler(ExceptionHandler exceptionHandler){
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        public Builder switchPolicy(SwitchPolicy switchPolicy){
            this.switchPolicy = switchPolicy;
            return this;
        }

        public Builder zookeeperServerDataResolver(ZookeeperServerDataResolver resolver){
            this.zookeeperServerDataResolver = resolver;
            return this;
        }

        public Builder zookeeperHostsGetter(ZookeeperHostsGetter zookeeperHostsGetter){
            this.zookeeperHostsGetter = zookeeperHostsGetter;
            return this;
        }

        public ProxyConfigure build(){
            return new ProxyConfigure(this);
        }

    }



}
