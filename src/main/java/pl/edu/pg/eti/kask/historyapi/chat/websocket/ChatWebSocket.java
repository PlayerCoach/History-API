package pl.edu.pg.eti.kask.historyapi.chat.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import pl.edu.pg.eti.kask.historyapi.chat.dto.ChatMessage;
import pl.edu.pg.eti.kask.historyapi.chat.event.ChatMessageEvent;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


@ApplicationScoped
@ServerEndpoint("/chat-websocket")
public class ChatWebSocket {

    private static final Logger LOGGER = Logger.getLogger(ChatWebSocket.class.getName());

    /**
     * WebSocket Map, Key is username, Value is Session.
     */
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();

    private final Jsonb jsonb = JsonbBuilder.create();

    @OnOpen
    public void onOpen(Session session) {
        String username = getUsernameFromSession(session);
        if (username != null && !username.isEmpty()) {
            sessions.put(username, session);
            LOGGER.info("WebSocket opened for user: " + username);

            broadcastUserList();
        }
    }


    @OnClose
    public void onClose(Session session) {
        String username = getUsernameFromSession(session);
        if (username != null) {
            sessions.remove(username);
            LOGGER.info("WebSocket closed for user: " + username);

            broadcastUserList();
        }
    }


    @OnError
    public void onError(Session session, Throwable throwable) {
        String username = getUsernameFromSession(session);
        LOGGER.log(Level.WARNING, "WebSocket error for user: " + username, throwable);
        sessions.remove(username);
    }

   // Optional
    @OnMessage
    public void onMessage(String message, Session session) {
        LOGGER.info("Received message via WebSocket: " + message);

    }

    public void onChatMessage(@Observes ChatMessageEvent event) {
        ChatMessage message = event.getMessage();
        String jsonMessage = jsonb.toJson(message);

        LOGGER.info("Broadcasting chat message from: " + message.getSender() +
                " to: " + (message.isPrivateMessage() ? message.getRecipient() : "ALL"));

        if (message.isPrivateMessage()) {
            sendToUser(message.getSender(), jsonMessage);
            if (!message.getSender().equals(message.getRecipient())) {
                sendToUser(message.getRecipient(), jsonMessage);
            }
        } else {
            broadcast(jsonMessage);
        }
    }

    private void broadcast(String message) {
        sessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to send message", e);
            }
        });
    }

    private void sendToUser(String username, String message) {
        Session session = sessions.get(username);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to send message to user: " + username, e);
            }
        }
    }

    private void broadcastUserList() {
        Set<String> onlineUsers = sessions.keySet();
        String userListJson = jsonb.toJson(Map.of("type", "userList", "users", onlineUsers));
        broadcast(userListJson);
    }

    private String getUsernameFromSession(Session session) {
        String query = session.getQueryString();
        if (query != null && query.contains("username=")) {
            return query.replace("username=", "");
        }
        return null;
    }

    public static Set<String> getOnlineUsers() {
        return sessions.keySet();
    }
}
