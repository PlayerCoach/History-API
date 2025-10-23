package pl.edu.pg.eti.kask.historyapi.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;

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

    @JsonIgnore
    private LocalDate birthday;

    @ToString.Exclude
    @JsonIgnore
    private String password;

    private String email;
    @JsonIgnore
    Role role;
    @JsonIgnore
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
