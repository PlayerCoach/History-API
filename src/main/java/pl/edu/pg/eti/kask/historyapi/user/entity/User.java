package pl.edu.pg.eti.kask.historyapi.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import lombok.*;
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
// ONLY use field-level @Exclude annotations, remove (exclude = "...")
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String login;

    @JsonIgnore
    @JsonbTransient
    private LocalDate birthday;

    @ToString.Exclude // New style: Excludes from toString()
    @EqualsAndHashCode.Exclude // New style: Excludes from equals/hashCode()
    @JsonIgnore
    @JsonbTransient
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @JsonbTransient
    @Enumerated(EnumType.STRING)
    Role role;

    @ToString.Exclude // New style: Excludes from toString()
    @EqualsAndHashCode.Exclude // New style: Excludes from equals/hashCode()
    @JsonIgnore
    @JsonbTransient
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, orphanRemoval = true)
    List<Note> createdNotes = new ArrayList<>(); // 1:N relationship

    /*
    Simplified constructor for the purpose of lab 1
     */
    public User(UUID id, String login, String email)
    {
        this.id = id;
        this.login = login;
        this.email = email;

    }

}
