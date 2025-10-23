package pl.edu.pg.eti.kask.historyapi.note.entity;

import lombok.*;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.entity.HistoricalFigure;
import pl.edu.pg.eti.kask.historyapi.note.entity.Mode;
import pl.edu.pg.eti.kask.historyapi.user.entity.User;

import java.io.Serializable;
import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor

@ToString
@EqualsAndHashCode

public class Note implements Serializable {

    private UUID id;
    private String title;
    private String content; // Cannot be empty!
    private Mode mode; // PRIVATE or PUBLIC

    private UUID historicalFigureId;; // Note about 1 historical figure
    private UUID userId; // Author of the note
}
