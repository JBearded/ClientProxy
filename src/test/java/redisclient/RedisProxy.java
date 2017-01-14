package redisclient;

import com.eproxy.ClosableClient;
import com.eproxy.EasyProxy;
import com.eproxy.ProxyConfigure;
import com.eproxy.ServerInfo;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

/**
 * @author xiejunquan
 * @create 2016/11/23 15:51
 */
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
