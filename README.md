# ClientProxy

## 简介
此项目用于多个服务的客户端代理，达到高可用的作用

## 使用

* 编写实现ClosableClient类的客户端
* 编写实现ServerInfoResolver类，用于获取服务端信息的列表（也可在ClientProxy中传入一个ServerInfo List）
* 运行代理类代码

实现ClosableClient

    public class RedisClient implements ClosableClient{

        private JedisPool jedisPool;

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

        public String setex(String key, String value, int seconds) {
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

运行代理类代码

    List<ServerInfo> serverInfos = new ArrayList<ServerInfo>();

    String ip1 = "127.0.0.1";
    int port1 = 6380;
    int weight1 = 2;

    String ip2 = "127.0.0.1";
    int port2 = 6381;
    int weight2 = 1;

    JedisPoolConfig config = new JedisPoolConfig();
    config.setMaxTotal(100);
    config.setMaxIdle(100);
    config.setMinIdle(20);
    config.setMaxWaitMillis(5000);
    ClientConstructor clientClientConstructor = new ClientConstructor(RedisClient.class, config, host, port, timeout);

    ServerInfo serverInfo1 = new ServerInfo(host1, port1, weight1, clientClientConstructor);
    ServerInfo serverInfo2 = new ServerInfo(host2, port2, weight2, clientClientConstructor);
    serverInfos.add(serverInfo1);
    serverInfos.add(serverInfo2);

    Configure configure = new Configure.Builder()
                    .checkServerAvailableIntervalMs(1000 * 60 * 10)
                    .loadBalanceStrategy(LoadBalanceStrategy.WRR)
                    .maxExceptionTimes(10)
                    .minExceptionFrequencyMs(1000 * 2)
                    .telnetTimeoutMs(1000 * 5)
                    .build();

    ClientProxy<RedisClient> redisProxy = new ClientProxy(serverInfos, configure);
    for (int i = 0; i < 100; i++) {
        Thread.sleep(5000);
        RedisClient client = redisProxy.getClient();
        if(client != null){
            client.setex("hello", "world" + i, 60);
        }
    }