package pl.edu.pg.eti.kask.historyapi.note.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;

import java.util.*;

@ApplicationScoped
public class NoteRepository {

    @PersistenceContext(unitName = "historyPU")
    private EntityManager em;

    public NoteRepository() {}

    public List<Note> findAll() {
        return em.createQuery("SELECT n FROM Note n", Note.class)
                .getResultList();
    }

    public Optional<Note> findById(UUID id) {
        return Optional.ofNullable(em.find(Note.class, id));
    }

    public List<Note> findByFigureId(UUID figureId) {
        return em.createQuery("SELECT n FROM Note n WHERE n.historicalFigure.id = :figureId", Note.class)
                .setParameter("figureId", figureId)
                .getResultList();
    }


    public void delete(UUID id) {
        Note note = em.find(Note.class, id);
        if (note != null) {
            em.remove(note);
        }
    }

    public void deleteNotesWithHistoricalFigureId(UUID historicalFigureId) {
        em.createQuery("DELETE FROM Note n WHERE n.historicalFigure.id = :figureId")
                .setParameter("figureId", historicalFigureId)
                .executeUpdate();
    }

    public void save(Note note) {
        if (note.getId() == null) {
            note.setId(UUID.randomUUID());
        }

        Note existing = em.find(Note.class, note.getId());
        if (existing == null) {
            em.persist(note);
            if (note.getHistoricalFigure() != null && !note.getHistoricalFigure().getNotes().contains(note)) {
                note.getHistoricalFigure().getNotes().add(note);
            }
        } else {
            em.merge(note);
        }
    }
}