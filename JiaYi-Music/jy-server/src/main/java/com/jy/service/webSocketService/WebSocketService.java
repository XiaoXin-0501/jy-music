package com.jy.service.webSocketService;

import com.jy.cache.RedissonService;
import com.jy.constant.ErrorMessage;
import com.jy.constant.WebSocketConstants;
import com.jy.exception.WebSocketException;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.redisson.api.RStream;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.stream.StreamMessageId;
import org.redisson.api.stream.StreamReadArgs;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@ServerEndpoint("/ws/{userId}")
@Component
public class WebSocketService {
    private static ConcurrentHashMap<String, Session> SESSION_MAP = new ConcurrentHashMap<>();
    private static RedissonClient redissonClient;
    private static RTopic topic;
    private static RStream<String, String> stream;
    private Session session;
    private String userId;

    public static void setRedisson(RedissonService redissonService) {
        redissonClient = redissonService.getRedisson();
        topic = redissonClient.getTopic(WebSocketConstants.GLOBAL_TOPIC);
        stream = redissonClient.getStream(WebSocketConstants.GLOBAL_STREAM);
        subscribeGlobalTopic();
        subscribeGlobalStream();
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.session = session;
        this.userId = userId;
        SESSION_MAP.put(userId, session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> data = objectMapper.readValue(
                    message,
                    new TypeReference<Map<String, String>>() {
                    }
            );
            if (WebSocketConstants.PING.equals(data.get(WebSocketConstants.CONNECT))) {
                Map<String, String> map = Map.of(WebSocketConstants.CONNECT, WebSocketConstants.PONG);
                sendText(map);
            }
        } catch (Exception e) {
            System.out.println("非心跳消息");
        }
    }

    @OnClose
    public void onClose(Session session) {
        if (!this.userId.isEmpty()) {
            SESSION_MAP.remove(this.userId);
        }
    }

    @OnError
    public void onError(Throwable error) {
        if (userId != null) {
            SESSION_MAP.remove(userId);
        }
//        throw new WebSocketException(ErrorMessage.WS_ERROR);
    }

    //发送给当前连接用户
    public void sendText(Map<String, String> message) {
        if (session == null || !session.isOpen()) {
            throw new WebSocketException(ErrorMessage.WS_SEND_FAIL);
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            session.getBasicRemote().sendText(objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            throw new WebSocketException(ErrorMessage.WS_SEND_FAIL);
        }
    }

    public void sendText(String message) {
        Map<String, String> map = Map.of(WebSocketConstants.DATA, message);
        sendText(map);
    }

    //发给指定用户
    public static void sendTextToUser(Map<String, String> message, String userId) {
        if (!SESSION_MAP.containsKey(userId)) {
            return;
        }
        Session session = SESSION_MAP.get(userId);
        if (session == null || !session.isOpen()) {
            throw new WebSocketException(ErrorMessage.WS_SEND_FAIL);
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            session.getBasicRemote().sendText(objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            throw new WebSocketException(ErrorMessage.WS_SEND_FAIL);
        }
    }

    public static void sendTextToUser(String message, String userId) {
        Map<String, String> map = Map.of(WebSocketConstants.DATA, message);
        sendTextToUser(map, userId);
    }

    //发给本机所有用户
    public static void sendTextToAll(Map<String, String> message) {
        ObjectMapper objectMapper = new ObjectMapper();
        for (Session session : SESSION_MAP.values()) {
            if (session != null && session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(objectMapper.writeValueAsString(message));
                } catch (Exception e) {
                    throw new WebSocketException(ErrorMessage.WS_SEND_ALL_FAIL);
                }
            }
        }
    }

    public static void sendTextToAll(String message) {
        Map<String, String> map = Map.of(WebSocketConstants.DATA, message);
        sendTextToAll(map);
    }

    private static void subscribeGlobalTopic() {
        // 监听消息（Redisson 自动重连）
        topic.addListener(String.class, (channel, msg) -> {
            try {
                String[] data = msg.split(Pattern.quote(WebSocketConstants.DELIMITER), 2);
                String prefix = data[0];
                String message = data[1];
                if (WebSocketConstants.GLOBAL_MESSAGE.equals(prefix)) {
                    sendTextToAll(message);
                } else {
                    sendTextToUser(message, prefix);
                }

            } catch (Exception ignored) {
                throw new WebSocketException(ErrorMessage.WS_TOPIC_EX);
            }
        });
    }

    private static void subscribeGlobalStream() {
        // 后台线程监听
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Map<StreamMessageId, Map<String, String>> messages =
                            stream.read(StreamReadArgs.greaterThan(StreamMessageId.ALL).count(10).timeout(Duration.ofSeconds(5)));
                    // 处理消息
                    for (var entry : messages.entrySet()) {
//                        StreamMessageId msgId = entry.getKey();
                        Map<String, String> data = entry.getValue();
                        // 业务处理：推送给WebSocket
                        String prefix = data.get(WebSocketConstants.PREFIX);
                        String content = data.get(WebSocketConstants.MESSAGE);
                        if (WebSocketConstants.GLOBAL_MESSAGE.equals(prefix)) {
                            sendTextToAll(content);
                        } else {
                            sendTextToUser(content, prefix);
                        }
                    }
                } catch (Exception e) {
                    //消费者仅自己，无需ack
                    throw new WebSocketException(ErrorMessage.WS_SEND_ALL_FAIL);
                }
            }
        }, WebSocketConstants.STREAM_THREAD).start();
    }

}
