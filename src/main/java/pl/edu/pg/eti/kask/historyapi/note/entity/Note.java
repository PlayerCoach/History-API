package pl.edu.pg.eti.kask.historyapi.note.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.entity.HistoricalFigure;
import pl.edu.pg.eti.kask.historyapi.user.entity.User;
import pl.edu.pg.eti.kask.historyapi.validation.NoProfanity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"historicalFigure", "createdBy"})
@EqualsAndHashCode(exclude = {"historicalFigure", "createdBy"})
@Entity
@Table(name = "notes")
public class Note implements Serializable {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @NotBlank(message = "{validation.note.title.notBlank}")
    @Size(min = 3, max = 100, message = "{validation.note.title.size}")
    @NoProfanity(message = "{validation.note.title.noProfanity}")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "{validation.note.content.notBlank}")
    @Size(min = 10, max = 5000, message = "{validation.note.content.size}")
    @Column(nullable = false)
    private String content;

    @NotNull(message = "{validation.note.mode.notNull}")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Mode mode;

    @JsonIgnore
    @JsonbTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historical_figure_id")
    private HistoricalFigure historicalFigure;

    @JsonIgnore
    @JsonbTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
