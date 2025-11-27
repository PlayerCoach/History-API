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

/**
 * WebSocket endpoint dla czatu.
 * Obsługuje połączenia WebSocket i rozsyła wiadomości do klientów.
 */
@ApplicationScoped
@ServerEndpoint("/chat-websocket")
public class ChatWebSocket {

    private static final Logger LOGGER = Logger.getLogger(ChatWebSocket.class.getName());

    /**
     * Mapa sesji WebSocket - klucz to nazwa użytkownika, wartość to sesja.
     */
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();

    /**
     * JSON-B do serializacji wiadomości.
     */
    private final Jsonb jsonb = JsonbBuilder.create();

    /**
     * Obsługuje otwarcie połączenia WebSocket.
     */
    @OnOpen
    public void onOpen(Session session) {
        String username = getUsernameFromSession(session);
        if (username != null && !username.isEmpty()) {
            sessions.put(username, session);
            LOGGER.info("WebSocket opened for user: " + username);

            // Wyślij powiadomienie o dołączeniu użytkownika
            broadcastUserList();
        }
    }

    /**
     * Obsługuje zamknięcie połączenia WebSocket.
     */
    @OnClose
    public void onClose(Session session) {
        String username = getUsernameFromSession(session);
        if (username != null) {
            sessions.remove(username);
            LOGGER.info("WebSocket closed for user: " + username);

            // Wyślij zaktualizowaną listę użytkowników
            broadcastUserList();
        }
    }

    /**
     * Obsługuje błędy WebSocket.
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        String username = getUsernameFromSession(session);
        LOGGER.log(Level.WARNING, "WebSocket error for user: " + username, throwable);
        sessions.remove(username);
    }

    /**
     * Obsługuje odebranie wiadomości przez WebSocket (opcjonalne - głównie używamy AJAX).
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        LOGGER.info("Received message via WebSocket: " + message);
        // Można tutaj obsłużyć wiadomości wysyłane bezpośrednio przez WebSocket
    }

    /**
     * Obserwator CDI Event - gdy zostanie wysłana wiadomość, rozsyła ją przez WebSocket.
     */
    public void onChatMessage(@Observes ChatMessageEvent event) {
        ChatMessage message = event.getMessage();
        String jsonMessage = jsonb.toJson(message);

        LOGGER.info("Broadcasting chat message from: " + message.getSender() +
                " to: " + (message.isPrivateMessage() ? message.getRecipient() : "ALL"));

        if (message.isPrivateMessage()) {
            // Wiadomość prywatna - wyślij tylko do nadawcy i odbiorcy
            sendToUser(message.getSender(), jsonMessage);
            if (!message.getSender().equals(message.getRecipient())) {
                sendToUser(message.getRecipient(), jsonMessage);
            }
        } else {
            // Wiadomość publiczna - wyślij do wszystkich
            broadcast(jsonMessage);
        }
    }

    /**
     * Wysyła wiadomość do wszystkich połączonych użytkowników.
     */
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

    /**
     * Wysyła wiadomość do konkretnego użytkownika.
     */
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

    /**
     * Rozsyła aktualną listę użytkowników online.
     */
    private void broadcastUserList() {
        Set<String> onlineUsers = sessions.keySet();
        String userListJson = jsonb.toJson(Map.of("type", "userList", "users", onlineUsers));
        broadcast(userListJson);
    }

    /**
     * Pobiera nazwę użytkownika z sesji WebSocket.
     */
    private String getUsernameFromSession(Session session) {
        // Nazwa użytkownika jest przekazywana jako query parameter
        String query = session.getQueryString();
        if (query != null && query.contains("username=")) {
            return query.replace("username=", "");
        }
        return null;
    }

    /**
     * Zwraca listę aktualnie połączonych użytkowników.
     */
    public static Set<String> getOnlineUsers() {
        return sessions.keySet();
    }
}
