package com.eproxy;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务的客户端信息
 * @author 谢俊权
 * @create 2016/9/2 15:59
 */
public abstract class ServerConfigureResolver {

    private static final Logger logger = LoggerFactory.getLogger(ServerConfigureResolver.class);

    private static final String ELEMENT_EPROXY = "eproxy";
    private static final String ELEMENT_GOUPID = "groupId";
    private static final String ELEMENT_SERVERNAME = "serverName";
    private static final String ELEMENT_HOSTS = "defaultHosts";
    private static final String ELEMENT_EXTENDINFO = "extendInfo";
    private static final String ELEMENT_PROPERTY = "property";
    private static final String ATTRIBUTE_KEY = "key";
    private static final String ATTRIBUTE_VALUE = "value";

    public static ServerConfigure get(String config) {
        Element root = getRoot(config);
        return getServerConfigure(root);
    }

    private static Element getRoot(String configFileName) {
        Element root = null;
        try {
            SAXReader reader = new SAXReader();
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            URL url = ClassLoader.getSystemResource(configFileName);
            Document document = reader.read(url);
            root = document.getRootElement();
        } catch (Exception e) {
            logger.error("error to read config:{}", configFileName, e);
        }
        return root;
    }

    private static ServerConfigure getServerConfigure(Element root){
        ServerConfigure serverConfigure = new ServerConfigure();
        if(root != null){
            long groupId = Long.valueOf(root.elementTextTrim(ELEMENT_GOUPID));
            String serverName = root.elementTextTrim(ELEMENT_SERVERNAME);
            String defaultHosts = root.elementTextTrim(ELEMENT_HOSTS);
            Element extendElement = root.element(ELEMENT_EXTENDINFO);
            List<Element> extendInfoPropertyList = extendElement.elements(ELEMENT_PROPERTY);
            Map<String, String> extendInfo = new HashMap<>();
            for (Element propertyElement : extendInfoPropertyList) {
                String key = propertyElement.attributeValue(ATTRIBUTE_KEY);
                String value = propertyElement.attributeValue(ATTRIBUTE_VALUE);
                extendInfo.put(key, value);
            }
            List<ServerInfo> serverInfoList = getServerInfoList(defaultHosts, extendInfo);
            serverConfigure.setGroupId(groupId);
            serverConfigure.setServerName(serverName);
            serverConfigure.setExtendInfo(extendInfo);
            serverConfigure.setServerInfoList(serverInfoList);
        }
        return serverConfigure;
    }

    private static List<ServerInfo> getServerInfoList(String hosts, Map<String, String> extendInfo){
        List<ServerInfo> serverInfoList = new ArrayList<>();
        if(hosts != null && !"".equals(hosts.trim())){
            String[] hostArray = hosts.split(";");
            for(String host : hostArray){
                ServerInfo serverInfo = new ServerInfo();
                String[] items = host.split(":");
                if(items.length >= 2){
                    String ip = items[0];
                    String port = items[1];
                    serverInfo.setIp(ip);
                    serverInfo.setPort(Integer.valueOf(port));
                    if(items.length >= 3){
                        serverInfo.setWeight(Integer.valueOf(items[2]));
                    }
                    serverInfo.setExtendInfoMap(extendInfo);
                    serverInfoList.add(serverInfo);
                }

            }
        }
        return serverInfoList;
    }
}
