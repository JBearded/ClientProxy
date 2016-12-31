import com.eproxy.Configure;
import com.eproxy.exception.DefaultExceptionHandler;
import com.eproxy.exception.DefaultSwitchPolicy;
import com.eproxy.loadbalance.LoadBalanceStrategy;
import org.junit.Test;
import redisclient.RedisClient;
import redisclient.RedisProxy;

/**
 * @author 谢俊权
 * @create 2016/5/6 10:25
 */
public class JunitTest {

    @Test
    public void redisTest() throws InterruptedException {


        Configure configure = new Configure.Builder()
                .checkServerAvailableIntervalMs(1000 * 10)
                .loadBalanceStrategy(LoadBalanceStrategy.WRR)
                .exceptionHandler(new DefaultExceptionHandler())
                .switchPolicy(new DefaultSwitchPolicy(1, 2))
                .build();

        RedisProxy redisProxy = new RedisProxy("redis.xml", configure);
        for (int i = 0; i < 10; i++) {
            RedisClient client = redisProxy.getClient();
            client.setex("hello", 60, "world" + i);
        }
    }
}
