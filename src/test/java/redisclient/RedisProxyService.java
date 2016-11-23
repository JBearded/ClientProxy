package redisclient;

import com.eproxy.EasyProxy;
import com.eproxy.Configure;

/**
 * @author xiejunquan
 * @create 2016/11/23 15:51
 */
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
