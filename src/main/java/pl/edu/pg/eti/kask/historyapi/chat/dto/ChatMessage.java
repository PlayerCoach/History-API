package pl.edu.pg.eti.kask.historyapi.chat.dto;

import jakarta.json.bind.annotation.JsonbDateFormat;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ChatMessage implements Serializable {

    private String sender;

    /**
     *  (null -> send to all).
     */
    private String recipient;

    private String content;

    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private boolean privateMessage;

    public boolean isFor(String username) {
        if (!privateMessage) {
            return true; // Wiadomość publiczna - dla wszystkich
        }
        return username.equals(recipient) || username.equals(sender);
    }
}
