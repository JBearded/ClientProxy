package com.eproxy.exception;

/**
 * 客户端报错之后的处理
 *
 * @author xiejunquan
 * @create 2016/11/23 14:36
 */
public interface ExceptionHandler {

    void handle(MethodExceptionInfo exceptionInfo);

}
