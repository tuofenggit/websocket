package com.wat.websocket.constant;

/**
 * @Author: chuangwang8
 * @Date: 2018-07-05 11:19
 * @Description: socket 状态码
 */
public class MessageCode {

    /**
     * 建立连接成功
     */
    public static String CONNECT_SUC = "0";

    /**
     * 在线用户通知
     */
    public static String LOGIN_NOTICE = "10";

    /**
     * 用户单线交流  服务器推送
     */
    public static String SEND_MESSAGE_TO = "20";
    /**
     * 用户单线交流  用户主动发送
     */
    public static String SEND_MESSAGE_FROM = "21";

    /**
     * 消息发送失败
     */
    public static String SEND_FAIL = "30";


    /**
     *群发
     */
    public static String SEND_ALL_USER_TO = "40";

    /**
     * 群发来源
     */
    public static String SEND_ALL_USER_FROM = "41";

}
