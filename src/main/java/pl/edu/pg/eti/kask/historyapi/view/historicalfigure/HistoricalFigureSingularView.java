package pl.edu.pg.eti.kask.historyapi.view.historicalfigure;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.SecurityContext;
import lombok.Getter;
import lombok.Setter;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.entity.HistoricalFigure;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.service.HistoricalFigureService;
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;
import pl.edu.pg.eti.kask.historyapi.note.service.NoteService;
import pl.edu.pg.eti.kask.historyapi.user.entity.User;
import pl.edu.pg.eti.kask.historyapi.user.service.UserService;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Named("figureDetailView")
@ViewScoped
public class HistoricalFigureSingularView implements Serializable {

    @Inject
    private HistoricalFigureService figureService;

    @Inject
    private NoteService noteService;

    @Inject
    private UserService userService;

    @Inject
    private SecurityContext securityContext;

    @Setter
    @Getter
    private UUID figureId;

    private HistoricalFigure figure;

    private List<Note> notes;

    public void loadData() {
        if (figureId != null) {
            figure = figureService.findById(figureId).orElse(null);
            notes = null; // Reset notes to force reload
        }
    }

    public String deleteNote(UUID noteId) {
        noteService.delete(noteId);
        notes = null;
        return "/historicalfigure/figure?faces-redirect=true&figureId=" + figureId;
    }

    public HistoricalFigure getFigure() {
        if (figure == null && figureId != null) {
            figure = figureService.findById(figureId).orElse(null);
        }
        return figure;
    }

    public List<Note> getNotes() {
        if (notes == null && figureId != null) {
            List<Note> allNotes = noteService.findByFigureId(figureId);

            // Administrator widzi wszystkie notatki, zwykły użytkownik tylko swoje
            if (securityContext.isCallerInRole("ADMIN")) {
                notes = allNotes;
            } else {
                String username = securityContext.getCallerPrincipal().getName();
                User currentUser = userService.findByLogin(username).orElse(null);
                if (currentUser != null) {
                    notes = allNotes.stream()
                            .filter(note -> note.getCreatedBy() != null &&
                                           note.getCreatedBy().getId().equals(currentUser.getId()))
                            .collect(Collectors.toList());
                } else {
                    notes = List.of();
                }
            }
        }
        return notes;
    }

}
