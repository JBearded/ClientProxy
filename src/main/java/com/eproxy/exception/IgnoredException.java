package com.eproxy.exception;

/**
 * 可忽略的报错, 客户端代理不处理此报错
 * @author 谢俊权
 * @create 2016/5/11 17:40
 */
public class IgnoredException extends RuntimeException{

    public IgnoredException(String message) {
        super(message);
    }

    public IgnoredException(Throwable cause) {
        super(cause);
    }

    public IgnoredException(String message, Throwable cause) {
        super(message, cause);
    }
}
