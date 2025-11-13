package pl.edu.pg.eti.kask.historyapi.note.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import lombok.*;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.entity.HistoricalFigure;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "historicalFigure")
@EqualsAndHashCode(exclude = "historicalFigure")
@Entity
@Table(name = "notes")
public class Note implements Serializable {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Mode mode;

    @JsonIgnore
    @JsonbTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historical_figure_id")
    private HistoricalFigure historicalFigure;

    @Column(name = "user_id")
    private UUID userId;

    // Helper method for JSON serialization
    @JsonProperty("historicalFigureId")
    public UUID getHistoricalFigureId() {
        return historicalFigure != null ? historicalFigure.getId() : null;
    }

    @JsonProperty("historicalFigureId")
    public void setHistoricalFigureId(UUID id) {
        // This will be handled by the controller/service layer
    }
}
