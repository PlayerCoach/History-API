package pl.edu.pg.eti.kask.historyapi.view.note;


import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.SecurityContext;
import lombok.Getter;
import lombok.Setter;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.entity.HistoricalFigure;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.service.HistoricalFigureService;
import pl.edu.pg.eti.kask.historyapi.note.entity.Mode;
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;
import pl.edu.pg.eti.kask.historyapi.note.service.NoteService;
import pl.edu.pg.eti.kask.historyapi.user.entity.User;
import pl.edu.pg.eti.kask.historyapi.user.service.UserService;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Named("noteListView")
@ViewScoped
public class NoteListView implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private transient NoteService noteService;

    @Inject
    private transient UserService userService;

    @Inject
    private transient HistoricalFigureService figureService;

    @Inject
    private transient SecurityContext securityContext;

    @Getter
    private List<Note> notes;

    // Pola filtrów
    @Getter @Setter
    private String filterTitle;

    @Getter @Setter
    private String filterContent;

    @Getter @Setter
    private Mode filterMode;

    private Map<UUID, String> userNameMap;
    private Map<UUID, String> figureNameMap;


    @PostConstruct
    public void init() {
        loadNotes();
    }


    public void loadNotes() {
        String ownerFilter = null;

        if (!securityContext.isCallerInRole("ADMIN")) {
            ownerFilter = securityContext.getCallerPrincipal().getName();
        }

        notes = noteService.findWithFilters(filterTitle, filterContent, filterMode, ownerFilter);
    }

    /**
     * Akcja filtrowania - wywoływana z formularza
     */
    public String filter() {
        loadNotes();
        return null;
    }

    /**
     * Czyści filtry i pokazuje wszystkie notatki
     */
    public String clearFilters() {
        filterTitle = null;
        filterContent = null;
        filterMode = null;
        loadNotes();
        return null;
    }

    public Mode[] getModes() {
        return Mode.values();
    }

    public String getUserName(UUID userId) {
        if (userNameMap == null) {
            userNameMap = userService.findAll().stream()
                    .collect(Collectors.toMap(User::getId, User::getLogin));
        }
        return userNameMap.getOrDefault(userId, "Nieznany Użytkownik");
    }

    public String getFigureName(UUID figureId) {
        if (figureNameMap == null) {
            figureNameMap = figureService.findAll().stream()
                    .collect(Collectors.toMap(HistoricalFigure::getId, HistoricalFigure::getName));
        }
        return figureNameMap.getOrDefault(figureId, "Nieznana Postać");
    }
}
