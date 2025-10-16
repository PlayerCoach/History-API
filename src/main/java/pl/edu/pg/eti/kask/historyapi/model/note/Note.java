package pl.edu.pg.eti.kask.historyapi.model.note;

import lombok.*;
import pl.edu.pg.eti.kask.historyapi.model.historicalfigure.HistoricalFigure;

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
    @Singular
    HistoricalFigure historicalFigure; // Note about 1 historical figure
}
