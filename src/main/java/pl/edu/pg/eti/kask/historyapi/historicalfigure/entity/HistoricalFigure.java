package pl.edu.pg.eti.kask.historyapi.historicalfigure.entity;


import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode


public class HistoricalFigure {
    private UUID id;
    private String name;
    private LocalDate dateOfBrith;
    private LocalDate dateOfDeath;


}
