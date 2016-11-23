package com.eproxy.exception;

import com.eproxy.ServerInfo;

import java.lang.reflect.Method;

/**
 * 方法报错的相关信息
 *
 * @author xiejunquan
 * @create 2016/11/23 14:42
 */
public class MethodExceptionInfo {

    private Throwable throwable;
    private ServerInfo serverInfo;
    private Object targetObject;
    private Method method;
    private Object[] args;

    public MethodExceptionInfo() {
    }

    public MethodExceptionInfo(Throwable throwable, ServerInfo serverInfo, Object targetObject, Method method, Object[] args) {
        this.throwable = throwable;
        this.serverInfo = serverInfo;
        this.targetObject = targetObject;
        this.method = method;
        this.args = args;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public Object getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(Object targetObject) {
        this.targetObject = targetObject;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
