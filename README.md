# EasyProxy

此项目是一个基于客户端的负载均衡代理, 代理通过RR,WRR等算法去配置的多个服务中选择一个作为访问目标, 而且如果有服务不可用, 代理也会自动做切换处理, 并定时检查不可用的服务是否已经可用.
假如你有一个服务部署了3个进程A、B、C, 当你正在发布A进程时(或A进程挂掉), 客户端代理就会去访问B和C, 直到A进程发布完成(或A进程恢复).

## 使用

为了使用这个代理, 你需要做以下工作
* 实现ClosableClient接口的客户端(此客户端必须要有一个空的构造器)
* 继承EasyProxy类, 实现create方法
* 配置服务列表信息

### 实现ClosableClient接口

    public class RedisClient implements ClosableClient{

        private JedisPool jedisPool;

        //必须有一个空构造器
        public RedisClient(){

        }

        public RedisClient(JedisPoolConfig config, String ip, int port, int timeout) {
            this.jedisPool = new JedisPool(config, ip, port, timeout);
        }

        public String set(String key, String value) {
            Jedis jedis = jedisPool.getResource();
            try{
                return jedis.set(key, value);
            }finally {
                jedis.close();
            }

        }

        public String setex(String key, int seconds, String value) {
            Jedis jedis = jedisPool.getResource();
            try{
                return jedis.setex(key, seconds, value);
            }finally {
                jedis.close();
            }

        }

        public String get(String key) {
            Jedis jedis = jedisPool.getResource();
            try{
                return jedis.get(key);
            }finally {
                jedis.close();
            }

        }

        @Override
        public void close() {
            try{
                jedisPool.destroy();
            }catch (Exception e){
                throw new IgnoredException(e);
            }

        }
    }

### 继承EasyProxy类

    public class RedisProxy extends EasyProxy<RedisClient> {

        public RedisProxy(String config) {
            super(config);
        }

        public RedisProxy(String config, ProxyConfigure proxyConfigure) {
            super(config, proxyConfigure);
        }

        @Override
        protected ClosableClient create(ServerInfo serverInfo) {
            Map<String, String> extendInfoMap = serverInfo.getExtendInfoMap();
            JedisPoolConfig config = new JedisPoolConfig();
            int timeout = Integer.valueOf(extendInfoMap.get("timeout"));
            config.setMaxTotal(Integer.valueOf(extendInfoMap.get("maxActive")));
            config.setMaxIdle(Integer.valueOf(extendInfoMap.get("maxIdle")));
            config.setMinIdle(Integer.valueOf(extendInfoMap.get("minIdle")));
            config.setMaxWaitMillis(timeout);
            RedisClient redisClient = new RedisClient(config, serverInfo.getIp(), serverInfo.getPort(), timeout);
            return redisClient;
        }
    }

### 配置服务列表信息

    <eproxy>
        <zookeeperHosts>192.168.126.128:2181</zookeeperHosts>
        <groupId>123</groupId>
        <serverName>redisServer</serverName>
        <defaultHosts>192.168.126.128:6380:1;192.168.126.128:6381:1</defaultHosts>
        <extendInfo>
            <property key="maxActive" value="100"></property>
            <property key="maxIdle" value="100"></property>
            <property key="minIdle" value="20"></property>
            <property key="timeout" value="10000"></property>
        </extendInfo>
    </eproxy>

## zookeeper动态更新服务列表

依赖第三方的调用服务常常会出现几种情况: 1、第三方服务添加新的机器 2、第三方服务所在机器被回收. 这就导致我们自己的服务也要跟着更新服务列表并重新发布. 为了避免这种情况的发生,
此代理支持使用zookeeper来做第三方服务列表监控, 并更新到自己的服务当中来.
看如上配置代码, <zookeeperHosts>即是zookeeper的服务集群host, 为了支持区分机房服务, 使用<groupId>来标识. 假如有2个机房A和B, 用123和456分别表示其groupId. 第三方服务通过
节点`/easy/proxy/{groupId}/{serverName}/{ip:port}`来注册服务信息到zookeeper中, 本机房的调用服务就会更新列表

## 调用测试

    @Test
    public void redisTest() throws InterruptedException {

        ProxyConfigure proxyConfigure = new ProxyConfigure.Builder()
                .checkServerAvailableIntervalMs(1000 * 10)
                .maxCountExceptionSecondTime(60)
                .maxExceptionTimes(5)
                .loadBalanceStrategy(LoadBalanceStrategy.WRR)
                .exceptionHandler(new DefaultExceptionHandler())
                .switchPolicy(new DefaultSwitchPolicy(1, 2))
                .zookeeperServerDataResolver(new DefaultZookeeperServerDataResolver())
                .build();

        RedisProxy redisProxy = new RedisProxy("redis-proxy.xml", proxyConfigure);
        for (int i = 0; i < 100; i++) {
            Thread.sleep(1000);
            RedisClient client = redisProxy.getClient();
            client.setex("hello", 60, "world" + i);
        }
    }