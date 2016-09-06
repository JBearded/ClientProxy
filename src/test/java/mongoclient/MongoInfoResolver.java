package mongoclient;

import com.bj.proxy.ClientConstructor;
import com.bj.proxy.ServerInfo;
import com.bj.proxy.ServerInfoResolver;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 谢俊权
 * @create 2016/9/2 16:19
 */
public class MongoInfoResolver extends ServerInfoResolver{

    private static final String MONGO = "mongo";

    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String DB = "db";
    private static final String WEIGHT = "weight";

    public MongoInfoResolver(String configPath) {
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
        List<Element> redisList = root.elements(MONGO);
        for (Element element : redisList) {
            ServerInfo serverInfo = getServerInfo(element);
            list.add(serverInfo);
        }
        return list;
    }

    private ServerInfo getServerInfo(Element element){
        String host = element.element(HOST).getStringValue();
        int port = Integer.valueOf(element.element(PORT).getStringValue());
        int weight = Integer.valueOf(element.element(WEIGHT).getStringValue());
        String db = element.element(DB).getStringValue();

        ClientConstructor clientConstructor = new ClientConstructor(
                MongoProxyClient.class,
                new ClientConstructor.Parameter(String.class, host),
                new ClientConstructor.Parameter(int.class, port),
                new ClientConstructor.Parameter(String.class, db)
        );
        ServerInfo serverInfo = new ServerInfo(host, port, weight, clientConstructor);
        return serverInfo;
    }
}
