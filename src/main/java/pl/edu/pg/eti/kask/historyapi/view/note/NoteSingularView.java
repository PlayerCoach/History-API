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
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;
import pl.edu.pg.eti.kask.historyapi.note.service.NoteService;
import pl.edu.pg.eti.kask.historyapi.user.entity.User;
import pl.edu.pg.eti.kask.historyapi.user.service.UserService;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

@Named("noteDetailView")
@ViewScoped
public class NoteSingularView implements Serializable {
    @Inject
    private NoteService noteService;

    @Inject
    private SecurityContext securityContext;

    @Inject
    private UserService userService;

    @Inject
    private FacesContext facesContext;

    @Getter
    @Setter
    private UUID noteId;

    private Note note;
    private HistoricalFigure figure;

    @PostConstruct
    public void init() {
        checkAccess();
    }

    public Note getNote() {
        if (note == null && noteId != null) {
            note = noteService.findById(noteId).orElse(null);
            checkAccess();
        }
        return note;
    }

    public HistoricalFigure getFigure() {
        if (figure == null && getNote() != null && getNote().getHistoricalFigure() != null) {
            figure = getNote().getHistoricalFigure();
        }
        return figure;
    }

    /**
     * Sprawdza czy użytkownik ma dostęp do notatki (właściciel lub admin)
     */
    private void checkAccess() {
        if (noteId != null && note != null) {
            // Administrator ma dostęp do wszystkiego
            if (securityContext.isCallerInRole("ADMIN")) {
                return;
            }

            // Sprawdź czy użytkownik jest właścicielem
            String username = securityContext.getCallerPrincipal().getName();
            User currentUser = userService.findByLogin(username).orElse(null);

            if (currentUser == null || note.getCreatedBy() == null ||
                !note.getCreatedBy().getId().equals(currentUser.getId())) {
                // Brak dostępu - przekieruj na stronę błędu
                try {
                    facesContext.getExternalContext().responseSendError(403, "Brak dostępu do tej notatki");
                    facesContext.responseComplete();
                } catch (IOException e) {
                    facesContext.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Błąd", "Nie masz dostępu do tej notatki"));
                }
            }
        }
    }
}
