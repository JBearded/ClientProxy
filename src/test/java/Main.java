import com.client.loadbalance.LoadBalanceStrategy;
import com.client.proxy.ClientProxy;
import com.client.proxy.Configure;
import redisclient.RedisClient;
import redisclient.RedisInfoResolver;

/**
 * @author 谢俊权
 * @create 2016/5/6 10:25
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {


        Configure configure = new Configure.Builder()
                .checkServerAvailableIntervalMs(1000 * 60 * 10)
                .loadBalanceStrategy(LoadBalanceStrategy.WRR)
                .maxExceptionTimes(10)
                .minExceptionFrequencyMs(1000 * 2)
                .telnetTimeoutMs(1000 * 5)
                .build();

//        ClientProxy mongoProxy = new ClientProxy(new RedisInfoResolver("mongo.xml"), configure);
//
//        for (int i = 0; i < 20; i++) {
//            Thread.sleep(5000);
//            MongoProxyClient client = (MongoProxyClient) mongoProxy.getClient();
//            client.insertOne("er", new Document().append("hello", i));
//        }



        ClientProxy<RedisClient> redisProxy = new ClientProxy(new RedisInfoResolver("redis.xml"), configure);
        for (int i = 0; i < 100; i++) {
            Thread.sleep(5000);
            RedisClient client = redisProxy.getClient();
            if(client != null){
                client.setex("hello", "world" + i, 60);
            }
        }
    }
}
