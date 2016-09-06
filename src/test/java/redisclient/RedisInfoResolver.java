package redisclient;

import com.bj.proxy.ClientConstructor;
import com.bj.proxy.ServerInfo;
import com.bj.proxy.ServerInfoResolver;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import redis.clients.jedis.JedisPoolConfig;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 谢俊权
 * @create 2016/9/2 16:08
 */
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
