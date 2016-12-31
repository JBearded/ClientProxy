package redisclient;

import com.eproxy.ServerInfo;
import com.eproxy.ServerInfoResolver;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 谢俊权
 * @create 2016/9/2 16:08
 */
public class RedisInfoResolver extends ServerInfoResolver {

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
        return getClientInfoList(root);
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

    private List<ServerInfo> getClientInfoList(Element root){

        List<ServerInfo> list = new ArrayList<ServerInfo>();
        List<Element> redisList = root.elements(REDIS);
        for (Element element : redisList) {
            ServerInfo serverInfo = getClientInfo(element);
            list.add(serverInfo);
        }
        return list;
    }

    private ServerInfo getClientInfo(Element element){

        String host = element.element(HOST).getStringValue();
        int port = Integer.valueOf(element.element(PORT).getStringValue());
        int maxActive = Integer.valueOf(element.element(MAXACTIVE).getStringValue());
        int maxIdle = Integer.valueOf(element.element(MAXIDLE).getStringValue());
        int minIdle = Integer.valueOf(element.element(MINIDLE).getStringValue());
        int timeout = Integer.valueOf(element.element(TIMEOUT).getStringValue());
        int weight = Integer.valueOf(element.element(WEIGHT).getStringValue());

        ServerInfo serverInfo = new ServerInfo(host, port, weight);
        serverInfo.setExtendInfo(MAXACTIVE, maxActive);
        serverInfo.setExtendInfo(MAXIDLE, maxIdle);
        serverInfo.setExtendInfo(MINIDLE, minIdle);
        serverInfo.setExtendInfo(MAXACTIVE, maxActive);
        serverInfo.setExtendInfo(TIMEOUT, timeout);
        return serverInfo;
    }

}
