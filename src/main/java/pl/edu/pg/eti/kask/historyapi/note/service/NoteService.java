package pl.edu.pg.eti.kask.historyapi.note.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;
import pl.edu.pg.eti.kask.historyapi.note.repository.NoteRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Stateless
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

    public List<Note> findByFigureIdAndOwner(UUID figureId, String username) {
        return repository.findByFigureIdAndOwner(figureId, username);
    }

    public List<Note> findByOwner(String username) {
        return repository.findByOwner(username);
    }


    public void deleteNotesWithHistoricalFigureId(UUID historicalFigureId) {
        repository.deleteNotesWithHistoricalFigureId(historicalFigureId);
    }

    public void delete(UUID id) {
        repository.delete(id);
    }

    public void save(Note note) {
        repository.save(note);
    }
}