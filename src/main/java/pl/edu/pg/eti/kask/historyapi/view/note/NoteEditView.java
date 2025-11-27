package pl.edu.pg.eti.kask.historyapi.view.note;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
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

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Named("noteEditView")
@ViewScoped
public class NoteEditView implements Serializable {

    @Inject
    private NoteService noteService;

    @Inject
    private HistoricalFigureService figureService;

    @Inject
    private UserService userService;

    @Inject
    private SecurityContext securityContext;

    @Inject
    private FacesContext facesContext;

    @Getter
    @Setter
    private UUID noteId;

    @Getter
    @Setter
    private UUID figureId;

    @Getter
    private Note note;

    private List<HistoricalFigure> availableFigures;

    @PostConstruct
    public void init() {
        loadData();
    }

    public void loadData() {
        if (note != null) {
            return;
        }

        if (noteId != null) {
            // Edycja istniejącej notatki
            note = noteService.findById(noteId).orElse(null);
            if (note != null) {
                checkAccess();
            }
        } else {
            // Tworzenie nowej notatki
            note = new Note();
            note.setMode(Mode.PUBLIC);

            // Ustaw aktualnego użytkownika jako twórcę
            String username = securityContext.getCallerPrincipal().getName();
            User currentUser = userService.findByLogin(username).orElse(null);
            note.setCreatedBy(currentUser);

            if (figureId != null) {
                HistoricalFigure figure = figureService.findById(figureId).orElse(null);
                note.setHistoricalFigure(figure);
            }
        }
    }

    public List<HistoricalFigure> getAvailableFigures() {
        if (availableFigures == null) {
            availableFigures = figureService.findAll();
        }
        return availableFigures;
    }

    public String save() {
        // Sprawdź dostęp przed zapisem
        if (noteId != null) {
            checkAccess();
        }

        // Jeśli to nowa notatka, ustaw ID
        if (note.getId() == null) {
            note.setId(UUID.randomUUID());
        }

        noteService.save(note);
        UUID figureIdForRedirect = (note.getHistoricalFigure() != null) ? note.getHistoricalFigure().getId() : null;
        return "/historicalfigure/figure?faces-redirect=true&figureId=" + figureIdForRedirect;
    }

    public Mode[] getModes() {
        return Mode.values();
    }

    /**
     * Sprawdza czy użytkownik ma dostęp do edycji notatki (właściciel lub admin)
     */
    private void checkAccess() {
        if (note != null && noteId != null) {
            // Administrator ma dostęp do wszystkiego
            if (securityContext.isCallerInRole("ADMIN")) {
                return;
            }

            // Sprawdź czy użytkownik jest właścicielem
            String username = securityContext.getCallerPrincipal().getName();
            User currentUser = userService.findByLogin(username).orElse(null);

            if (currentUser == null || note.getCreatedBy() == null ||
                !note.getCreatedBy().getId().equals(currentUser.getId())) {
                // Brak dostępu - przekieruj na listę notatek
                try {
                    String contextPath = facesContext.getExternalContext().getRequestContextPath();
                    facesContext.getExternalContext().redirect(contextPath + "/note/notes.xhtml");
                } catch (IOException e) {
                    facesContext.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Błąd", "Nie masz dostępu do edycji tej notatki"));
                }
            }
        }
    }
}