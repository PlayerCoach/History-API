package pl.edu.pg.eti.kask.historyapi.historicalfigure.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.entity.HistoricalFigure;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.repository.HistoricalFigureRepository;
import pl.edu.pg.eti.kask.historyapi.note.service.NoteService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Stateless
public class HistoricalFigureService {

    @Inject
    private HistoricalFigureRepository repository;

    @Inject
    private NoteService noteService;

    public HistoricalFigureService() {}

    public List<HistoricalFigure> findAll() {
        return repository.findAll();
    }

    public Optional<HistoricalFigure> findById(UUID id) {
        return repository.findById(id);
    }


    public void delete(UUID id) {
        // First, delete all notes associated with the historical figure
        noteService.deleteNotesWithHistoricalFigureId(id);
        // Then, delete the historical figure itself
        repository.delete(id);
    }

    public void save(HistoricalFigure historicalFigure) {
        repository.save(historicalFigure);
    }
}