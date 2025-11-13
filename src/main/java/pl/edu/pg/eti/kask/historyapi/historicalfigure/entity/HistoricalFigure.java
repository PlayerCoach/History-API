package pl.edu.pg.eti.kask.historyapi.historicalfigure.entity;


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
@ToString(exclude = "notes")
@EqualsAndHashCode(exclude = "notes")
@Entity
@Table(name = "historical_figures")
public class HistoricalFigure implements Serializable {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBrith;

    @Column(name = "date_of_death")
    private LocalDate dateOfDeath;

    @JsonIgnore
    @JsonbTransient
    @OneToMany(mappedBy = "historicalFigure", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Note> notes = new ArrayList<>();

}
