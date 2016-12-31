package redisclient;

import com.eproxy.ClosableClient;
import com.eproxy.EasyProxy;
import com.eproxy.Configure;
import com.eproxy.ServerInfo;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

/**
 * @author xiejunquan
 * @create 2016/11/23 15:51
 */
public class RedisProxy extends EasyProxy<RedisClient> {


    @Override
    protected ClosableClient create(ServerInfo serverInfo) {
        Map<String, Object> extendInfoMap = serverInfo.getExtendInfoMap();
        JedisPoolConfig config = new JedisPoolConfig();
        int timeout = (Integer) extendInfoMap.get("timeout");
        config.setMaxTotal((Integer) extendInfoMap.get("maxActive"));
        config.setMaxIdle((Integer) extendInfoMap.get("maxIdle"));
        config.setMinIdle((Integer) extendInfoMap.get("minIdle"));
        config.setMaxWaitMillis(timeout);
        RedisClient redisClient = new RedisClient(config, serverInfo.getIp(), serverInfo.getPort(), timeout);
        return redisClient;
    }

    public RedisProxy(String config) {
        super(new RedisInfoResolver(config));
    }

    public RedisProxy(String config, Configure configure) {
        super(new RedisInfoResolver(config), configure);
    }

}
