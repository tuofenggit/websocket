package com.wat.websocket.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * @Auther: chuangwang8
 * @Date: 2018-07-04 14:11
 * @Description:
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    MyWebSocketHandler myWebSocketHandler;

    @Autowired
    HandshakeInterceptor handshakeInterceptor;


    public WebSocketConfig() {
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        System.err.println(" WebSocketConfig registerWebSocketHandlers  registed!");

        registry.addHandler(myWebSocketHandler, "/websck").addInterceptors(handshakeInterceptor);
        registry.addHandler(myWebSocketHandler, "/sockjs/websck/info").addInterceptors(new HandshakeInterceptor())
                .withSockJS();
    }

}
