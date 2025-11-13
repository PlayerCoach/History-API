package pl.edu.pg.eti.kask.historyapi.note.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;
import pl.edu.pg.eti.kask.historyapi.note.repository.NoteRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class NoteService {

    @Inject
    private NoteRepository repository;

    public NoteService() {}

    public List<Note> findAll() {
        return repository.findAll();
    }

    public Optional<Note> findById(UUID id) {
        return repository.findById(id);
    }

    public List<Note> findByFigureId(UUID figureId) {
        return repository.findByFigureId(figureId);
    }


    @Transactional
    public void deleteNotesWithHistoricalFigureId(UUID historicalFigureId) {
        repository.deleteNotesWithHistoricalFigureId(historicalFigureId);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(id);
    }

    @Transactional
    public void save(Note note) {
        repository.save(note);
    }
}