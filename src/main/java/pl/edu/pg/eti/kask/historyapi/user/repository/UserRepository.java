package pl.edu.pg.eti.kask.historyapi.user.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import pl.edu.pg.eti.kask.historyapi.user.entity.User;

import java.util.*;

@Stateless
public class UserRepository {

    @PersistenceContext(unitName = "historyPU")
    private EntityManager em;

    public List<User> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        cq.select(root);
        return em.createQuery(cq).getResultList();
    }

    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    public Optional<User> findByLogin(String login) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        cq.select(root).where(cb.equal(root.get("login"), login));
        return em.createQuery(cq).getResultStream().findFirst();
    }

    public void save(User user) {
        User existing = em.find(User.class, user.getId());
        if (existing == null) {
            em.persist(user);
        } else {
            em.merge(user);
        }
    }

    public void delete(UUID id) {
        findById(id).ifPresent(em::remove);
    }
}