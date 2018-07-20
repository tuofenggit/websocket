package com.wat.websocket.config;

import com.alibaba.fastjson.JSONObject;
import com.wat.websocket.utils.WebSocketUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @Author: chuangwang8
 * @Date: 2018-07-04 14:13
 * @Description:
 */

@Component
public class MyWebSocketHandler implements WebSocketHandler {

    Logger logger = LogManager.getLogger();

    public static final Map<Long, Set<WebSocketSession>> userSocketSessionMap;

    static {
        userSocketSessionMap = new ConcurrentHashMap<>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            Long uid = Long.valueOf(session.getAttributes().get("uid") + "");
            logger.info("uid ::" + uid);
            if (userSocketSessionMap.get(uid) == null) {
                Set<WebSocketSession> webSocketSessions = new HashSet<>();
                webSocketSessions.add(session);
                userSocketSessionMap.put(uid, webSocketSessions);
            } else {
                Set<WebSocketSession> webSocketSessions = userSocketSessionMap.get(uid);
                webSocketSessions.add(session);
                userSocketSessionMap.put(uid, webSocketSessions);
            }

            Iterator<Map.Entry<Long, Set<WebSocketSession>>> it = userSocketSessionMap.entrySet().iterator();

            while (it.hasNext()) {
                int conunt = 0;
                Map.Entry<Long, Set<WebSocketSession>> entry = it.next();
                Set<WebSocketSession> webSocketSessions = entry.getValue();
                if (webSocketSessions != null && webSocketSessions.size() > 0) {
                    Iterator<WebSocketSession> sessionIterator = webSocketSessions.iterator();
                    while (sessionIterator.hasNext()) {
                        sessionIterator.next();
                        conunt++;
                    }
                } else {
                    it.remove();
                }

                logger.info("userSocketSessionMap size : " + userSocketSessionMap.size() + " , 用户（key）：["
                        + entry.getKey() + "] " + "共有 [" + conunt + " ]个存活的 session");

            }
            /**
             * 更新在线用户信息
             */
            WebSocketUtil.sendOnLineUsers(userSocketSessionMap);
            logger.info("userSocketSessionMap size : " + userSocketSessionMap.size());
            logger.info("connect to the websocket success...... userSocketSessionMap");
            JSONObject json = new JSONObject();
            json.put("code", "0");
            json.put("message", "succ");
            json.put("userList", userSocketSessionMap.keySet());

            if (session.isOpen()) {
                session.sendMessage(new TextMessage(json.toString()));
            }

        } catch (Exception e) {
            logger.info("建立连接异常：" + e);
            JSONObject json = new JSONObject();
            json.put("code", "1");
            json.put("message", "建立连接异常");
            session.sendMessage(new TextMessage(json.toString()));
        }

    }

    /**
     * @methdName: handleMessage
     * @param: [session, message]
     * @return: void
     * @Description: 处理接受到消息
     * @author: chuangwang8
     * @date: 2018-07-05
     * @version: V1.0
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        WebSocketUtil.sendSocketMessage(session, message, userSocketSessionMap);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable thrwbl) throws Exception {
        if (session.isOpen()) {
            session.close();
        }
        WebSocketUtil.delClosedSession(session, userSocketSessionMap);
        /**
         * 更新在线用户信息
         */
        WebSocketUtil.sendOnLineUsers(userSocketSessionMap);
        logger.info("websocket handleTransportError  connection closed......");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus cs) throws Exception {
        if (session.isOpen()) {
            session.close();
        }
        System.out.println("Websocket:" + session.getId() + "已经关闭");
        WebSocketUtil.delClosedSession(session, userSocketSessionMap);
        /**
         * 更新在线用户信息
         */
        WebSocketUtil.sendOnLineUsers(userSocketSessionMap);
        logger.info("websocket afterConnectionClosed connection closed......");
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }


}
