package com.bj.proxy;

import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * 客户端的工厂
 * @author 谢俊权
 * @create 2016/5/6 17:46
 */
public class ClientFactory {

    private Configure configure;

    public ClientFactory(Configure configure) {
        this.configure = configure;
    }

    public ClosableClient create(ClientInfo clientInfo){

        ClientConstructor clientConstructor = clientInfo.getClientConstructor();
        Class clientClazz = clientConstructor.getClientClass();
        List<Object> params = clientConstructor.getParameters();

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clientClazz);
        ClientHandler handler = new ClientHandler(clientInfo, configure);
        enhancer.setCallback(handler);

        ClosableClient result = null;
        if(params.isEmpty()){
            result = (ClosableClient) enhancer.create();
        }else{
            Class<?>[] types = getConstructorTypes(clientConstructor);
            Object[] values = params.toArray();
            result = (ClosableClient) enhancer.create(types, values);
        }
        return result;
    }

    private Class[] getConstructorTypes(ClientConstructor clientConstructor){
        Class clientClazz = clientConstructor.getClientClass();
        Constructor[] constructors = clientClazz.getDeclaredConstructors();
        List<Object> params = clientConstructor.getParameters();


        for (Constructor constructor : constructors) {
            boolean isSame = true;
            Class<?>[] types = constructor.getParameterTypes();
            if(types.length == params.size()){
                for (int i = 0; i < types.length; i++) {
                    Class<?> type = types[i];
                    Object value = params.get(i);
                    if(!isSameType(type, value)){
                        isSame = false;
                        break;
                    }
                }
            }
            if(isSame){
                return types;
            }
        }
        return null;
    }

    private boolean isSameType(Class<?> type, Object value){
        Class<?> clazz = value.getClass();
        if(!type.isPrimitive()){
            return type.equals(clazz);
        }else{
            Class<?> warpper =
                    (type.equals(byte.class)) ? Byte.class :
                            (type.equals(boolean.class)) ? Boolean.class :
                                    (type.equals(char.class)) ? Character.class :
                                            (type.equals(short.class)) ? Short.class :
                                                    (type.equals(int.class)) ? Integer.class :
                                                            (type.equals(long.class)) ? Long.class :
                                                                    (type.equals(float.class)) ? Float.class :
                                                                            (type.equals(double.class)) ? Double.class :
                                                                                    Object.class;


            return warpper.equals(clazz);
        }
    }

}
