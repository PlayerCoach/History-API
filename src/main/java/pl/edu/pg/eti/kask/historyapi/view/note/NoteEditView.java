package pl.edu.pg.eti.kask.historyapi.view.note;

import jakarta.ejb.EJBException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.OptimisticLockException;
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

    private static final long serialVersionUID = 1L;

    @Inject
    private transient NoteService noteService;

    @Inject
    private transient HistoricalFigureService figureService;

    @Inject
    private transient UserService userService;

    @Inject
    private transient SecurityContext securityContext;

    @Getter
    @Setter
    private UUID noteId;

    @Getter
    @Setter
    private UUID figureId;

    @Getter
    private Note note;

    /**
     * Aktualna wersja z bazy (pokazywana przy konflikcie)
     */
    @Getter
    private Note currentDatabaseNote;

    /**
     * Dane wprowadzone przez użytkownika (pokazywane przy konflikcie)
     */
    @Getter
    private Note userEnteredNote;

    /**
     * Flaga oznaczająca wystąpienie konfliktu wersji
     */
    @Getter
    private boolean versionConflict = false;

    private List<HistoricalFigure> availableFigures;

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

        try {
            noteService.save(note);
            UUID figureIdForRedirect = (note.getHistoricalFigure() != null) ? note.getHistoricalFigure().getId() : null;
            return "/historicalfigure/figure?faces-redirect=true&figureId=" + figureIdForRedirect;
        } catch (OptimisticLockException e) {
            handleVersionConflict();
            return null;
        } catch (EJBException e) {
            // OptimisticLockException jest opakowany w EJBException
            if (isOptimisticLockException(e)) {
                handleVersionConflict();
                return null;
            }
            throw e;
        }
    }

    /**
     * Obsługuje konflikt wersji - zapisuje dane użytkownika i pobiera aktualne z bazy
     */
    private void handleVersionConflict() {
        versionConflict = true;

        // Zachowaj dane wprowadzone przez użytkownika
        userEnteredNote = new Note();
        userEnteredNote.setTitle(note.getTitle());
        userEnteredNote.setContent(note.getContent());
        userEnteredNote.setMode(note.getMode());
        userEnteredNote.setVersion(note.getVersion());

        // Pobierz aktualne dane z bazy
        currentDatabaseNote = noteService.findById(note.getId()).orElse(null);

        // Zaktualizuj wersję w formularzu do aktualnej
        if (currentDatabaseNote != null) {
            note.setVersion(currentDatabaseNote.getVersion());
        }

        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Konflikt wersji", null));
    }

    /**
     * Sprawdza czy wyjątek zawiera OptimisticLockException w łańcuchu przyczyn
     */
    private boolean isOptimisticLockException(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof OptimisticLockException) {
                return true;
            }
            // EclipseLink używa własnej klasy OptimisticLockException
            if (cause.getClass().getName().contains("OptimisticLockException")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    /**
     * Przyjmuje aktualną wersję z bazy (nadpisuje zmiany użytkownika)
     */
    public String acceptCurrentVersion() {
        if (currentDatabaseNote != null) {
            note.setTitle(currentDatabaseNote.getTitle());
            note.setContent(currentDatabaseNote.getContent());
            note.setMode(currentDatabaseNote.getMode());
            note.setVersion(currentDatabaseNote.getVersion());
        }
        versionConflict = false;
        currentDatabaseNote = null;
        userEnteredNote = null;
        return null;
    }

    /**
     * Próbuje ponownie zapisać dane użytkownika z aktualną wersją
     */
    public String retryWithUserData() {
        if (userEnteredNote != null && currentDatabaseNote != null) {
            note.setTitle(userEnteredNote.getTitle());
            note.setContent(userEnteredNote.getContent());
            note.setMode(userEnteredNote.getMode());
            note.setVersion(currentDatabaseNote.getVersion());
        }
        versionConflict = false;
        currentDatabaseNote = null;
        userEnteredNote = null;
        return save();
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
                    FacesContext fc = FacesContext.getCurrentInstance();
                    String contextPath = fc.getExternalContext().getRequestContextPath();
                    fc.getExternalContext().redirect(contextPath + "/note/notes.xhtml");
                } catch (IOException e) {
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Błąd", "Nie masz dostępu do edycji tej notatki"));
                }
            }
        }
    }
}