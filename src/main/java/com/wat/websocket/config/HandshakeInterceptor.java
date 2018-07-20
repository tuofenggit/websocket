package com.wat.websocket.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author: chuangwang8
 * @Date: 2018-07-04 14:09
 * @Description:
 */
@Component
public class HandshakeInterceptor extends HttpSessionHandshakeInterceptor {
    Logger logger = LogManager.getLogger();
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        String uid = servletRequest.getParameter("uid");
        attributes.put("uid",uid);
        logger.info("beforeHandshake 发送到用户uid:" + uid);
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception ex) {
        System.err.println("After Handshake");
        super.afterHandshake(request, response, wsHandler, ex);
    }
}
