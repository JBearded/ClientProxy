import com.eproxy.Configure;
import com.eproxy.loadbalance.LoadBalanceStrategy;
import org.junit.Test;
import redisclient.RedisProxyService;

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
                .maxExceptionTimes(10)
                .minExceptionFrequencyMs(1000 * 2)
                .telnetTimeoutMs(1000 * 5)
                .build();

        RedisProxyService proxyService = new RedisProxyService("redis.xml", configure);
        for (int i = 0; i < 100; i++) {
            Thread.sleep(1000);
            proxyService.setex("hello", "world" + i, 60);
        }
    }
}
