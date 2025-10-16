package pl.edu.pg.eti.kask.historyapi.model.user;

import lombok.*;
import pl.edu.pg.eti.kask.historyapi.model.note.Note;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter

@NoArgsConstructor
@ToString
@EqualsAndHashCode

public class User implements Serializable {
    private UUID id;
    private String login;

    private LocalDate birthday;

    @ToString.Exclude
    private String password;

    private String email;
    Role role;
    List<Note> createdNotes; // 1:N relationship

    /*
    Simplified constructor for the purpose of lab 1
     */
    public User(UUID id, String login, String email)
    {
        this.id = id;
        this.login = login;
        this.email = email;
        
    }

    public String ToJsonString() {
        return "{" +
                "\"id\":\"" + id + '\"' +
                ", \"login\":\"" + login + '\"' +
                ", \"email\":\"" + email + '\"' +
                '}';
    }
}
