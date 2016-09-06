import com.bj.loadbalance.LoadBalanceStrategy;
import com.bj.proxy.ClientProxy;
import com.bj.proxy.Configure;
import mongoclient.MongoProxyClient;
import org.bson.Document;
import redisclient.RedisClient;
import redisclient.RedisInfoResolver;

/**
 * @author 谢俊权
 * @create 2016/5/6 10:25
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {


        Configure.Builder builder = new Configure.Builder();
        builder.checkServerAvailableIntervalMs(1000 * 60 * 10);
        builder.loadBalanceStrategy(LoadBalanceStrategy.WRR);
        builder.maxExceptionTimes(10);
        builder.minExceptionFrequencyMs(1000 * 2);
        builder.telnetTimeoutMs(1000 * 5);
        Configure configure = builder.build();


        ClientProxy mongoProxy = new ClientProxy(new RedisInfoResolver("mongo.xml"), configure);

        for (int i = 0; i < 20; i++) {
            Thread.sleep(5000);
            MongoProxyClient client = (MongoProxyClient) mongoProxy.getClient();
            client.insertOne("er", new Document().append("hello", i));
        }



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
