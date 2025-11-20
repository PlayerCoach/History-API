package pl.edu.pg.eti.kask.historyapi.user.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pl.edu.pg.eti.kask.historyapi.user.entity.User;

import java.util.*;

@Stateless
public class UserRepository {

    @PersistenceContext(unitName = "historyPU")
    private EntityManager em;

    public List<User> findAll() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    public Optional<User> findByLogin(String login) {
        return em.createQuery("SELECT u FROM User u WHERE u.login = :login", User.class)
                .setParameter("login", login)
                .getResultStream()
                .findFirst();
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