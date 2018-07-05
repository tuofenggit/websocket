package com.wat.websocket.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wat.websocket.constant.MessageCode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.wat.websocket.constant.MessageCode.*;

/**
 * @Auther: chuangwang8
 * @Date: 2018-07-05 14:28
 * @Description: socket util
 */
public class WebSocketUtil {


    private static Log logger = LogFactory.getLog(WebSocketUtil.class);

    /**
     * @methdName: sendSocketMessage
     * @param: [session, message, userSocketSessionMap]
     * @return: void
     * @Description: 用户交流发送信息
     * @author: chuangwang8
     * @date: 2018-07-05
     * @version: V1.0
     */
    public static void sendSocketMessage(WebSocketSession session, WebSocketMessage<?> message, Map<Long, Set<WebSocketSession>> userSocketSessionMap)
            throws IOException {

        JSONObject object = JSONObject.parseObject(message.getPayload() + "");
        String messageStr = object.get("message").toString();
        String toUid = object.get("toUid").toString();
        String code = object.get("code").toString();
        /**
         * 构造发送信息json
         */
        Map<String, String> jsonMap = new HashMap<>();

        /**
         * 单人发送
         */
        if (SEND_MESSAGE_FROM.equals(code)) {
            jsonMap.put("code", MessageCode.SEND_MESSAGE_TO);
            jsonMap.put("message", messageStr);
            TextMessage sendToUserMessage = new TextMessage(JSON.toJSONString(jsonMap));
            if (sendMessageToUser(Long.parseLong(toUid), sendToUserMessage, userSocketSessionMap) == 1) {
                /**
                 * 向消息发送者通知消息发送失败.
                 */
                sendFailInfo(session);
            }
        }

        /**
         * 群发
         */
        if (SEND_ALL_USER_FROM.equals(code)) {
            jsonMap.put("code", MessageCode.SEND_ALL_USER_TO);
            jsonMap.put("message", messageStr);
            TextMessage sendToUserMessage = new TextMessage(JSON.toJSONString(jsonMap));
            try {
                sendAllUserInfos(sendToUserMessage, userSocketSessionMap);
            } catch (IOException e) {
                sendFailInfo(session);
                logger.info("群发消息失败，异常信息： " + e);
            }
        }
    }

    /**
     * @methdName: sendFailInfo
     * @param: [webSocketSession]
     * @return: void
     * @Description: 发送失败
     * @author: chuangwang8
     * @date: 2018-07-05
     * @version: V1.0
     */
    public static void sendFailInfo(WebSocketSession webSocketSession) throws IOException {
        Map<String, String> messageStr = new HashMap<>();
        messageStr.put("code", SEND_FAIL);
        messageStr.put("message", "消息发送失败 [{(>_<)]} ");
        TextMessage returnMessage = new TextMessage(JSON.toJSONString(messageStr));
        if (webSocketSession.isOpen()) {
            webSocketSession.sendMessage(returnMessage);
        }
    }

    /**
     * @methdName: delOldSession
     * @param: [session]
     * @return: void
     * @Description: 从map 中删除已经关闭的 seesion
     * @author: chuangwang8
     * @date: 2018-07-04
     * @version: V1.0
     */
    public static void delClosedSession(WebSocketSession session, Map<Long, Set<WebSocketSession>> userSocketSessionMap) {

        Iterator<Map.Entry<Long, Set<WebSocketSession>>> it = userSocketSessionMap
                .entrySet().iterator();
        // 移除Socket会话
        while (it.hasNext()) {
            Map.Entry<Long, Set<WebSocketSession>> entry = it.next();

            Set<WebSocketSession> webSocketSessions = entry.getValue();

            if (webSocketSessions != null && webSocketSessions.size() > 0) {

                Iterator<WebSocketSession> sessionIterator = webSocketSessions.iterator();

                while (sessionIterator.hasNext()) {
                    /**
                     * 此处必须用，相当于 for循环的 i++
                     */
                    WebSocketSession webSocketSession = sessionIterator.next();

                    if (webSocketSession.getId().equals(session.getId())) {
                        logger.info("从map 中删除用户 uid 为[ " + entry.getKey() + " ]关闭的session id : [" + session.getId() + " ]");
                        sessionIterator.remove();
                    }
                }

            } else {
                it.remove();
                ;
            }

        }

    }


    /**
     * @methdName: sendAllUserInfos
     * @param: [message]
     * @return: void
     * @Description: 群发消息
     * @author: chuangwang8
     * @date: 2018-07-05
     * @version: V1.0
     */
    public static void sendAllUserInfos(final TextMessage message, Map<Long, Set<WebSocketSession>> userSocketSessionMap) throws IOException {
        Iterator<Map.Entry<Long, Set<WebSocketSession>>> userSeesions = userSocketSessionMap.entrySet().iterator();
        while (userSeesions.hasNext()) {
            final Map.Entry<Long, Set<WebSocketSession>> entry = userSeesions.next();
            Set<WebSocketSession> webSocketSessions = entry.getValue();
            if (webSocketSessions != null && webSocketSessions.size() > 0) {
                Iterator<WebSocketSession> sessionIterator = webSocketSessions.iterator();
                while (sessionIterator.hasNext()) {
                    final WebSocketSession session = sessionIterator.next();
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                /**
                                 * 如果session是存活状态，则发送更新信息
                                 */
                                if (session.isOpen()) {
                                    session.sendMessage(message);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                }
            } else {
                userSeesions.remove();
            }

        }
    }


    /**
     * @methdName: sendOnLineUsers
     * @param: []
     * @return: void
     * @Description: TODO
     * @author: chuangwang8
     * @date: 2018-07-05
     * @version: V1.0
     */
    public static void sendOnLineUsers(Map<Long, Set<WebSocketSession>> userSocketSessionMap) throws IOException {
        Iterator<Map.Entry<Long, Set<WebSocketSession>>> userSeesions = userSocketSessionMap.entrySet().iterator();
        while (userSeesions.hasNext()) {

            Map.Entry<Long, Set<WebSocketSession>> entry = userSeesions.next();
            Set<WebSocketSession> webSocketSessions = entry.getValue();

            if (webSocketSessions != null && webSocketSessions.size() > 0) {
                Iterator<WebSocketSession> sessionIterator = webSocketSessions.iterator();
                while (sessionIterator.hasNext()) {
                    WebSocketSession session = sessionIterator.next();
                    /**
                     * 如果session是存活状态，则发送更新信息
                     */
                    JSONObject json = new JSONObject();
                    json.put("code", "10");
                    json.put("message", "succ");
                    json.put("userList", userSocketSessionMap.keySet());
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(json.toString()));
                    }
                }
            } else {
                userSeesions.remove();
            }

        }
    }


    /**
     * 给某个用户发送消息
     * 0 成功，1 用户不存在
     *
     * @param uid
     * @param message
     * @throws IOException
     */
    public static int sendMessageToUser(Long uid, TextMessage message, Map<Long, Set<WebSocketSession>> userSocketSessionMap)
            throws IOException {
        Set<WebSocketSession> webSocketSessions = userSocketSessionMap.get(uid);
        if (webSocketSessions != null && webSocketSessions.size() > 0) {
            for (WebSocketSession session : webSocketSessions) {
                if (session != null && session.isOpen()) {
                    if (session.isOpen()) {
                        session.sendMessage(message);
                    }
                }
            }
            return 0;
        }
        return 1;
    }
}
