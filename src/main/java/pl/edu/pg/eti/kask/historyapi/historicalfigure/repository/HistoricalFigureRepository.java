package pl.edu.pg.eti.kask.historyapi.historicalfigure.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.entity.HistoricalFigure;

import java.util.*;

@Stateless
public class HistoricalFigureRepository {

    @PersistenceContext(unitName = "historyPU")
    private EntityManager em;

    public HistoricalFigureRepository() {}

    public List<HistoricalFigure> findAll() {
        return em.createQuery("SELECT f FROM HistoricalFigure f", HistoricalFigure.class)
                .getResultList();
    }

    public Optional<HistoricalFigure> findById(UUID id) {
        return Optional.ofNullable(em.find(HistoricalFigure.class, id));
    }

    public void delete(UUID id) {
        HistoricalFigure figure = em.find(HistoricalFigure.class, id);
        if (figure != null) {
            em.remove(figure);
        }
    }

    public void save(HistoricalFigure historicalFigure) {
        if (em.find(HistoricalFigure.class, historicalFigure.getId()) == null) {
            em.persist(historicalFigure);
        } else {
            em.merge(historicalFigure);
        }
    }
}
