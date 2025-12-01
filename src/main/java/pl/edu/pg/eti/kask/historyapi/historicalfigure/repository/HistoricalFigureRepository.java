package pl.edu.pg.eti.kask.historyapi.historicalfigure.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.entity.HistoricalFigure;

import java.util.*;

@Stateless
public class HistoricalFigureRepository {

    @PersistenceContext(unitName = "historyPU")
    private EntityManager em;

    public HistoricalFigureRepository() {}

    public List<HistoricalFigure> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<HistoricalFigure> cq = cb.createQuery(HistoricalFigure.class);
        Root<HistoricalFigure> root = cq.from(HistoricalFigure.class);
        cq.select(root);
        return em.createQuery(cq).getResultList();
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
