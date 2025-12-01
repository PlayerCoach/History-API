package pl.edu.pg.eti.kask.historyapi.note.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import pl.edu.pg.eti.kask.historyapi.note.entity.Mode;
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;

import java.util.*;

@Stateless
public class NoteRepository {

    @PersistenceContext(unitName = "historyPU")
    private EntityManager em;

    public NoteRepository() {}

    public List<Note> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Note> cq = cb.createQuery(Note.class);
        Root<Note> root = cq.from(Note.class);
        cq.select(root);
        return em.createQuery(cq).getResultList();
    }

    /**
     * Dynamiczne filtrowanie notatek za pomocą Criteria API.
     * Wszystkie parametry są opcjonalne i łączone operatorem AND.
     */
    public List<Note> findWithFilters(String title, String content, Mode mode, String ownerLogin) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Note> cq = cb.createQuery(Note.class);
        Root<Note> root = cq.from(Note.class);

        List<Predicate> predicates = new ArrayList<>();

        if (title != null && !title.isBlank()) {
            // Filtrowanie: tytuł zaczyna się od podanej frazy (case-insensitive)
            predicates.add(cb.like(cb.lower(root.get("title")), title.toLowerCase() + "%"));
        }

        if (content != null && !content.isBlank()) {
            // Filtrowanie: treść zaczyna się od podanej frazy (case-insensitive)
            predicates.add(cb.like(cb.lower(root.get("content")), content.toLowerCase() + "%"));
        }

        if (mode != null) {
            predicates.add(cb.equal(root.get("mode"), mode));
        }

        if (ownerLogin != null && !ownerLogin.isBlank()) {
            predicates.add(cb.equal(root.get("createdBy").get("login"), ownerLogin));
        }

        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        cq.select(root);
        return em.createQuery(cq).getResultList();
    }

    public Optional<Note> findById(UUID id) {
        return Optional.ofNullable(em.find(Note.class, id));
    }

    public List<Note> findByFigureId(UUID figureId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Note> cq = cb.createQuery(Note.class);
        Root<Note> root = cq.from(Note.class);
        cq.select(root).where(cb.equal(root.get("historicalFigure").get("id"), figureId));
        return em.createQuery(cq).getResultList();
    }

    public List<Note> findByFigureIdAndOwner(UUID figureId, String username) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Note> cq = cb.createQuery(Note.class);
        Root<Note> root = cq.from(Note.class);
        cq.select(root).where(
            cb.and(
                cb.equal(root.get("historicalFigure").get("id"), figureId),
                cb.equal(root.get("createdBy").get("login"), username)
            )
        );
        return em.createQuery(cq).getResultList();
    }

    public List<Note> findByOwner(String username) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Note> cq = cb.createQuery(Note.class);
        Root<Note> root = cq.from(Note.class);
        cq.select(root).where(cb.equal(root.get("createdBy").get("login"), username));
        return em.createQuery(cq).getResultList();
    }

    public void delete(UUID id) {
        Note note = em.find(Note.class, id);
        if (note != null) {
            em.remove(note);
        }
    }

    public void deleteNotesWithHistoricalFigureId(UUID historicalFigureId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<Note> cd = cb.createCriteriaDelete(Note.class);
        Root<Note> root = cd.from(Note.class);
        cd.where(cb.equal(root.get("historicalFigure").get("id"), historicalFigureId));
        em.createQuery(cd).executeUpdate();
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