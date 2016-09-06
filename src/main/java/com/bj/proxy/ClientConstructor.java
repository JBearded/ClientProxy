package com.bj.proxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author 谢俊权
 * @create 2016/9/2 15:04
 */
public class ClientConstructor {

    private Class<? extends ClosableClient> clientClass;
    private List<Parameter> parameters = new ArrayList<Parameter>();

    public ClientConstructor(Class<? extends ClosableClient> clientClass, Parameter... parameters) {
        this.clientClass = clientClass;
        this.parameters.addAll(Arrays.asList(parameters));
    }

    public Class<? extends ClosableClient> getClientClass() {
        return clientClass;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public static class Parameter{
        private Class<?> type;
        private Object value;

        public Parameter(Class<?> type, Object value) {
            this.type = type;
            this.value = value;
        }

        public Class<?> getType() {
            return type;
        }

        public Object getValue() {
            return value;
        }
    }
}
