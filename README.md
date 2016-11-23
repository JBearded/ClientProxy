# EasyProxy

## 简介
此项目用于多个服务的客户端代理，达到高可用的作用

## 使用

* 编写实现ClosableClient类的客户端
* 编写实现ServerInfoResolver类，用于获取服务端信息的列表（也可在EasyProxy中传入一个ServerInfo List）
* 运行代理类代码

### 实现ClosableClient

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

### 实现ServerInfoResolver(在junit测试中, 解析xml的信息)

redis.xml(配置多个redis服务)

    <?xml version="1.0" encoding="UTF-8"?>
    <redises>
        <redis>
            <host>127.0.0.1</host>
            <port>6380</port>
            <maxActive>100</maxActive>
            <maxIdle>100</maxIdle>
            <minIdle>20</minIdle>
            <timeout>10000</timeout>
            <weight>1</weight>
        </redis>
        <redis>
            <host>127.0.0.1</host>
            <port>6381</port>
            <maxActive>100</maxActive>
            <maxIdle>100</maxIdle>
            <minIdle>20</minIdle>
            <timeout>10000</timeout>
            <weight>1</weight>
        </redis>
    </redises>

ServerInfoResolver解析xml, 接口方法返回List<ServerInfo>

    public class RedisInfoResolver extends ServerInfoResolver {

        private static final String REDIS = "redis";

        private static final String HOST = "host";
        private static final String PORT = "port";
        private static final String MAXACTIVE = "maxActive";
        private static final String MAXIDLE = "maxIdle";
        private static final String MINIDLE = "minIdle";
        private static final String TIMEOUT = "timeout";
        private static final String WEIGHT = "weight";


        public RedisInfoResolver(String configPath) {
            super(configPath);
        }

        @Override
        public List<ServerInfo> get() {
            Element root = getRoot(configPath);
            return getClientInfoList(root);
        }

        private Element getRoot(String configFileName) {

            Element root = null;
            try {
                SAXReader reader = new SAXReader();
                URL url = ClassLoader.getSystemResource(configFileName);
                Document document = reader.read(url);
                root = document.getRootElement();
            } catch (DocumentException e) {
                e.printStackTrace();
            }

            return root;
        }

        private List<ServerInfo> getClientInfoList(Element root){

            List<ServerInfo> list = new ArrayList<ServerInfo>();
            List<Element> redisList = root.elements(REDIS);
            for (Element element : redisList) {
                ServerInfo serverInfo = getClientInfo(element);
                list.add(serverInfo);
            }
            return list;
        }

        private ServerInfo getClientInfo(Element element){

            String host = element.element(HOST).getStringValue();
            int port = Integer.valueOf(element.element(PORT).getStringValue());
            int maxActive = Integer.valueOf(element.element(MAXACTIVE).getStringValue());
            int maxIdle = Integer.valueOf(element.element(MAXIDLE).getStringValue());
            int minIdle = Integer.valueOf(element.element(MINIDLE).getStringValue());
            int timeout = Integer.valueOf(element.element(TIMEOUT).getStringValue());
            int weight = Integer.valueOf(element.element(WEIGHT).getStringValue());

            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(maxActive);
            config.setMaxIdle(maxIdle);
            config.setMinIdle(minIdle);
            config.setMaxWaitMillis(timeout);
            ClientConstructor clientClientConstructor = new ClientConstructor(
                    RedisClient.class,
                    config, host, port, timeout
            );


            ServerInfo serverInfo = new ServerInfo(host, port, weight, clientClientConstructor);
            return serverInfo;
        }

    }

### 运行代理类代码

    Configure configure = new Configure.Builder()
                    .checkServerAvailableIntervalMs(1000 * 60 * 10)
                    .loadBalanceStrategy(LoadBalanceStrategy.WRR)
                    .maxExceptionTimes(10)
                    .minExceptionFrequencyMs(1000 * 2)
                    .telnetTimeoutMs(1000 * 5)
                    .build();

    EasyProxy<RedisClient> redisProxy = new EasyProxy(new RedisInfoResolver("redis.xml") , configure);
    for (int i = 0; i < 100; i++) {
        Thread.sleep(5000);
        RedisClient client = redisProxy.getClient();
        if(client != null){
            client.setex("hello", "world" + i, 60);
        }
    }

为了使用起来更加便捷, 可以对EasyProxy再做一成包装(把代理和客户端组合在一起, 切换客户端的操作对开发者完全透明)

    public class RedisProxyService extends EasyProxy<RedisClient> {

        public RedisProxyService(String config) {
            super(new RedisInfoResolver(config));
        }

        public RedisProxyService(String config, Configure configure) {
            super(new RedisInfoResolver(config), configure);
        }

        public String set(String key, String value) {
            return getClient().set(key, value);
        }

        public String setex(String key, String value, int seconds) {
            return getClient().setex(key, seconds, value);
        }

        public String get(String key) {
            return getClient().get(key);
        }
    }

测试如下

    Configure configure = new Configure.Builder()
                    .checkServerAvailableIntervalMs(1000 * 10)
                    .loadBalanceStrategy(LoadBalanceStrategy.WRR)
                    .maxExceptionTimes(10)
                    .minExceptionFrequencyMs(1000 * 2)
                    .telnetTimeoutMs(1000 * 5)
                    .build();

    RedisProxyService proxyService = new RedisProxyService("redis.xml", configure);
    for (int i = 0; i < 100; i++) {
        Thread.sleep(1000);
        proxyService.setex("hello", "world" + i, 60);
    }

在执行期间, 你可以随意关闭其中一个redis服务, 看proxy是否会进行切换


* 这里只是使用redis作为一个测例, 你完全可以写自己的mongo, mysql的客户端代理.
* 默认地, 此代理服务不保证数据的完整性, 即调用失败就失败了. 你也可以实现ExceptionHandler在调用失败后做你想要的操作(默认只处理是否做切换连接服务的操作)
