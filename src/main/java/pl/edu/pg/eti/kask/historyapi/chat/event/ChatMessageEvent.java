package pl.edu.pg.eti.kask.historyapi.chat.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.pg.eti.kask.historyapi.chat.dto.ChatMessage;

import java.io.Serializable;

/**
 * CDI Event reprezentujące wysłanie wiadomości czatu.
 * Gdy użytkownik wysyła wiadomość, tworzony jest ten event,
 * który jest następnie przetwarzany przez obserwatora wysyłającego
 * wiadomość przez WebSocket.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageEvent implements Serializable {

    /**
     * Wiadomość czatu do wysłania.
     */
    private ChatMessage message;
}
