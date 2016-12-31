package redisclient;

import com.eproxy.exception.IgnoredException;
import com.eproxy.ClosableClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author 谢俊权
 * @create 2016/5/17 11:05
 */
public class RedisClient implements ClosableClient{

    private JedisPool jedisPool;

    public RedisClient(){

    }

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
