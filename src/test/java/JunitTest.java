import com.eproxy.ProxyConfigure;
import com.eproxy.exception.DefaultExceptionHandler;
import com.eproxy.exception.DefaultSwitchPolicy;
import com.eproxy.loadbalance.LoadBalanceStrategy;
import com.eproxy.zookeeper.DefaultZookeeperServerDataResolver;
import com.eproxy.zookeeper.ZookeeperHostsGetter;
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

        ProxyConfigure proxyConfigure = new ProxyConfigure.Builder()
                .checkServerAvailableIntervalMs(1000 * 10)
                .maxCountExceptionSecondTime(60)
                .maxExceptionTimes(5)
                .loadBalanceStrategy(LoadBalanceStrategy.WRR)
                .exceptionHandler(new DefaultExceptionHandler())
                .switchPolicy(new DefaultSwitchPolicy(1, 2))
                .zookeeperServerDataResolver(new DefaultZookeeperServerDataResolver())
                .zookeeperHostsGetter(new ZookeeperHostsGetter() {
                    @Override
                    public String get(long groupId) {
                        return "192.168.126.128:2181";
                    }
                })
                .build();

        RedisProxy redisProxy = new RedisProxy("redis-proxy.xml", proxyConfigure);
        for (int i = 0; i < 100; i++) {
            Thread.sleep(1000);
            RedisClient client = redisProxy.getClient();
            client.setex("hello", 60, "world" + i);
        }
    }
}
