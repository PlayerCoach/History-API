package pl.edu.pg.eti.kask.historyapi.view.historicalfigure;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.entity.HistoricalFigure;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.service.HistoricalFigureService;
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;
import pl.edu.pg.eti.kask.historyapi.note.service.NoteService;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Named("figureDetailView")
@ViewScoped
public class HistoricalFigureSingularView implements Serializable {



        @Inject
        private HistoricalFigureService figureService;

        @Inject
        private NoteService noteService;

        @Setter
        @Getter
        private UUID figureId;

        private HistoricalFigure figure;

        private List<Note> notes;

        public void loadData() {
            if (figureId != null) {
                figure = figureService.findById(figureId).orElse(null);

                notes = noteService.findByFigureId(figureId);
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
            notes = noteService.findByFigureId(figureId);
        }
        return notes;
    }


}
