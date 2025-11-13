package pl.edu.pg.eti.kask.historyapi.view.note;


import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.entity.HistoricalFigure;
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;
import pl.edu.pg.eti.kask.historyapi.note.service.NoteService;

import java.io.Serializable;
import java.util.UUID;

@Named("noteDetailView")
@ViewScoped
public class NoteSingularView implements Serializable {
    @Inject
    private NoteService noteService;


    @Getter
    @Setter
    private UUID noteId;

    private Note note;
    private HistoricalFigure figure;


    public Note getNote() {
        if (note == null && noteId != null) {
            note = noteService.findById(noteId).orElse(null);
        }
        return note;
    }


    public HistoricalFigure getFigure() {
        if (figure == null && getNote() != null && getNote().getHistoricalFigure() != null) {
            figure = getNote().getHistoricalFigure();
        }
        return figure;
    }

}
