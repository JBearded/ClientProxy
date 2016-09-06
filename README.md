# ClientProxy

## 简介
此项目用于多个服务的客户端代理，达到高可用的作用

## 使用

* 编写实现ClosableClient类的客户端
* 编写实现ServerInfoResolver类，用于获取服务端信息的列表（也可写死List, 此项目中的测试代码通过解析xml文件来获取服务端信息列表）
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

实现ServerInfoResolver

    public class RedisInfoResolver extends ServerInfoResolver{

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
            return getServerInfoList(root);
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

        private List<ServerInfo> getServerInfoList(Element root){

            List<ServerInfo> list = new ArrayList<ServerInfo>();
            List<Element> redisList = root.elements(REDIS);
            for (Element element : redisList) {
                ServerInfo serverInfo = getServerInfo(element);
                list.add(serverInfo);
            }
            return list;
        }

        private ServerInfo getServerInfo(Element element){

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
                    new ClientConstructor.Parameter(JedisPoolConfig.class, config),
                    new ClientConstructor.Parameter(String.class, host),
                    new ClientConstructor.Parameter(int.class, port),
                    new ClientConstructor.Parameter(int.class, timeout)
            );


            ServerInfo serverInfo = new ServerInfo(host, port, weight, clientClientConstructor);
            return serverInfo;
        }

    }

运行代理类代码

    Configure configure = new Configure.Builder()
                    .checkServerAvailableIntervalMs(1000 * 60 * 10)
                    .loadBalanceStrategy(LoadBalanceStrategy.WRR)
                    .maxExceptionTimes(10)
                    .minExceptionFrequencyMs(1000 * 2)
                    .telnetTimeoutMs(1000 * 5)
                    .build();

            ClientProxy<RedisClient> redisProxy = new ClientProxy(new RedisInfoResolver("redis.xml"), configure);
            for (int i = 0; i < 100; i++) {
                Thread.sleep(5000);
                RedisClient client = redisProxy.getClient();
                if(client != null){
                    client.setex("hello", "world" + i, 60);
                }
            }