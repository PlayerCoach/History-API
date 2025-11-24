package pl.edu.pg.eti.kask.historyapi.view.note;


import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.SecurityContext;
import lombok.Getter;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.entity.HistoricalFigure;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.service.HistoricalFigureService;
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;
import pl.edu.pg.eti.kask.historyapi.note.service.NoteService;
import pl.edu.pg.eti.kask.historyapi.user.entity.User;
import pl.edu.pg.eti.kask.historyapi.user.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Named("noteListView")
@RequestScoped
public class NoteListView {

    @Inject
    private NoteService noteService;

    @Inject
    private UserService userService;

    @Inject
    private HistoricalFigureService figureService;

    @Inject
    private SecurityContext securityContext;

    @Getter
    private List<Note> notes;
    private Map<UUID, String> userNameMap;
    private Map<UUID, String> figureNameMap;


    @PostConstruct
    public void init() {
        // Administrator widzi wszystkie notatki, zwykły użytkownik tylko swoje
        if (securityContext.isCallerInRole("ADMIN")) {
            notes = noteService.findAll();
        } else {
            String username = securityContext.getCallerPrincipal().getName();
            User currentUser = userService.findByLogin(username).orElse(null);
            if (currentUser != null) {
                notes = noteService.findAll().stream()
                        .filter(note -> note.getCreatedBy() != null &&
                                       note.getCreatedBy().getId().equals(currentUser.getId()))
                        .collect(Collectors.toList());
            } else {
                notes = List.of();
            }
        }

        userNameMap = userService.findAll().stream()
                .collect(Collectors.toMap(User::getId, User::getLogin));

        figureNameMap = figureService.findAll().stream()
                .collect(Collectors.toMap(HistoricalFigure::getId, HistoricalFigure::getName));
    }


    public String getUserName(UUID userId) {
        return userNameMap.getOrDefault(userId, "Nieznany Użytkownik");
    }

    public String getFigureName(UUID figureId) {
        return figureNameMap.getOrDefault(figureId, "Nieznana Postać");
    }
}
