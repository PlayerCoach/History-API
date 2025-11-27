package pl.edu.pg.eti.kask.historyapi.chat.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.pg.eti.kask.historyapi.chat.dto.ChatMessage;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageEvent implements Serializable {

    private ChatMessage message;
}
