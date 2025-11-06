package pl.edu.pg.eti.kask.historyapi.note.repository;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import pl.edu.pg.eti.kask.historyapi.note.entity.Mode;
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class NoteRepository {

    private final Map<UUID, Note> notes = new ConcurrentHashMap<>();


    public NoteRepository() {}

    @PostConstruct
    private void init() {

        UUID napoleonId = UUID.fromString("15a7f9a0-7ac1-11eb-8000-0242ac110002");
        UUID curieId = UUID.fromString("15a7fae0-7ac1-11eb-8001-0242ac110002");
        UUID testUserId = UUID.fromString("fe003ce8-0dae-46cb-8d01-104d1d91d4a0");

        Note n1 = new Note();
        n1.setId(UUID.fromString("25b8c1f0-7ac1-11eb-8000-0242ac110002"));
        n1.setTitle("Bitwa pod Waterloo");
        n1.setContent("Ostateczna porażka Napoleona.");
        n1.setMode(Mode.PUBLIC);
        n1.setHistoricalFigureId(napoleonId);
        n1.setUserId(testUserId);

        Note n2 = new Note();
        n2.setId(UUID.randomUUID());
        n2.setTitle("Odkrycie Polonu");
        n2.setContent("Pierwiastek nazwany na cześć Polski.");
        n2.setMode(Mode.PUBLIC);
        n2.setHistoricalFigureId(curieId);
        n2.setUserId(testUserId);

        Note n3 = new Note();
        n3.setId(UUID.randomUUID());
        n3.setTitle("Odkrycie Radu");
        n3.setContent("Kolejne wielkie odkrycie.");
        n3.setMode(Mode.PRIVATE);
        n3.setHistoricalFigureId(curieId);
        n3.setUserId(testUserId);

        notes.put(n1.getId(), n1);
        notes.put(n2.getId(), n2);
        notes.put(n3.getId(), n3);
    }

    public List<Note> findAll() {
        return new ArrayList<>(notes.values());
    }

    public Optional<Note> findById(UUID id) {
        return Optional.ofNullable(notes.get(id));
    }
    public List<Note> findByFigureId(UUID figureId) {
        return notes.values().stream()
                .filter(note -> note.getHistoricalFigureId().equals(figureId))
                .collect(Collectors.toList());
    }

    public List<Note> findByUserId(UUID userId) {
        return notes.values().stream()
                .filter(note -> note.getUserId().equals(userId))
                .collect(Collectors.toList());
    }


    public void delete(UUID id) {
        notes.remove(id);
    }

    public void deleteNotesWithHistoricalFigureId(UUID historicalFigureId) {
        notes.values().removeIf(note -> note.getHistoricalFigureId().equals(historicalFigureId));
    }

    public void save(Note note) {

        if(note.getId() == null) {
            note.setId(UUID.randomUUID());
        }
        notes.put(note.getId(), note);
    }


}