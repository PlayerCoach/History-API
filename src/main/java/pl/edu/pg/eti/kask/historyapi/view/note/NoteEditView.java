package pl.edu.pg.eti.kask.historyapi.view.note;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.entity.HistoricalFigure;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.service.HistoricalFigureService;
import pl.edu.pg.eti.kask.historyapi.note.entity.Mode;
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;
import pl.edu.pg.eti.kask.historyapi.note.service.NoteService;
import pl.edu.pg.eti.kask.historyapi.user.service.UserService;

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

    @Getter
    @Setter
    private UUID noteId;

    @Getter
    @Setter
    private UUID figureId;

    @Getter
    private Note note;

    private List<HistoricalFigure> availableFigures;


    public void loadData() {
        if (note != null) {
            return;
        }

        if (noteId != null) {
            note = noteService.findById(noteId).orElse(null);
        } else {

            note = new Note();
            note.setMode(Mode.PUBLIC);
            UUID testUserId = UUID.fromString("fe003ce8-0dae-46cb-8d01-104d1d91d4a0");
            note.setUserId(testUserId);

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
        noteService.save(note);
        UUID figureIdForRedirect = (note.getHistoricalFigure() != null) ? note.getHistoricalFigure().getId() : null;
        return "/historicalfigure/figure?faces-redirect=true&figureId=" + figureIdForRedirect;
    }

    public Mode[] getModes() {
        return Mode.values();
    }

}