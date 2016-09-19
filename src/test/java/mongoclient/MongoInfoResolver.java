package mongoclient;

import com.client.proxy.ClientConstructor;
import com.client.proxy.ClientInfo;
import com.client.proxy.ClientInfoResolver;
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
public class MongoInfoResolver extends ClientInfoResolver {

    private static final String MONGO = "mongo";

    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String DB = "db";
    private static final String WEIGHT = "weight";

    public MongoInfoResolver(String configPath) {
        super(configPath);
    }

    @Override
    public List<ClientInfo> get() {
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

    private List<ClientInfo> getClientInfoList(Element root){

        List<ClientInfo> list = new ArrayList<ClientInfo>();
        List<Element> redisList = root.elements(MONGO);
        for (Element element : redisList) {
            ClientInfo clientInfo = getClientInfo(element);
            list.add(clientInfo);
        }
        return list;
    }

    private ClientInfo getClientInfo(Element element){
        String host = element.element(HOST).getStringValue();
        int port = Integer.valueOf(element.element(PORT).getStringValue());
        int weight = Integer.valueOf(element.element(WEIGHT).getStringValue());
        String db = element.element(DB).getStringValue();

        ClientConstructor clientConstructor = new ClientConstructor(
                MongoProxyClient.class,
                host, port, db
        );
        ClientInfo clientInfo = new ClientInfo(host, port, weight, clientConstructor);
        return clientInfo;
    }
}
