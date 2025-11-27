package pl.edu.pg.eti.kask.historyapi.view.chat;

import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Event;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.SecurityContext;
import lombok.Getter;
import lombok.Setter;
import pl.edu.pg.eti.kask.historyapi.chat.dto.ChatMessage;
import pl.edu.pg.eti.kask.historyapi.chat.event.ChatMessageEvent;
import pl.edu.pg.eti.kask.historyapi.chat.websocket.ChatWebSocket;
import pl.edu.pg.eti.kask.historyapi.user.entity.User;
import pl.edu.pg.eti.kask.historyapi.user.service.UserService;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * JSF View obsługujący funkcjonalność czatu.
 * Wysyłanie wiadomości przez AJAX, odbieranie przez WebSocket.
 */
@Named
@SessionScoped
public class ChatView implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(ChatView.class.getName());

    @Inject
    private SecurityContext securityContext;

    @Inject
    private UserService userService;

    @Inject
    private Event<ChatMessageEvent> chatMessageEvent;

    @Getter
    @Setter
    private String messageContent;

    @Getter
    @Setter
    private String selectedRecipient;

    public String getCurrentUsername() {
        if (securityContext.getCallerPrincipal() != null) {
            return securityContext.getCallerPrincipal().getName();
        }
        return null;
    }

    public void sendMessage() {
        String sender = getCurrentUsername();

        if (sender == null) {
            addErrorMessage("Musisz być zalogowany, aby wysłać wiadomość.");
            return;
        }

        if (messageContent == null || messageContent.trim().isEmpty()) {
            addErrorMessage("Treść wiadomości nie może być pusta.");
            return;
        }

        boolean isPrivate = selectedRecipient != null && !selectedRecipient.trim().isEmpty()
                && !"ALL".equals(selectedRecipient);

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .recipient(isPrivate ? selectedRecipient : null)
                .content(messageContent.trim())
                .timestamp(LocalDateTime.now())
                .privateMessage(isPrivate)
                .build();

        LOGGER.info("Sending chat message from " + sender +
                " to " + (isPrivate ? selectedRecipient : "ALL") +
                ": " + messageContent);

        chatMessageEvent.fire(new ChatMessageEvent(message));
        messageContent = "";
    }

    public List<User> getAllUsers() {
        String currentUser = getCurrentUsername();
        return userService.findAll().stream()
                .filter(user -> !user.getLogin().equals(currentUser))
                .collect(Collectors.toList());
    }

    public Set<String> getOnlineUsers() {
        return ChatWebSocket.getOnlineUsers();
    }

    public boolean isUserOnline(String username) {
        return ChatWebSocket.getOnlineUsers().contains(username);
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }
}
