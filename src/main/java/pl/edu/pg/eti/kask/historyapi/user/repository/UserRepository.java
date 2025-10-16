package pl.edu.pg.eti.kask.historyapi.user.repository;

import pl.edu.pg.eti.kask.historyapi.user.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserRepository {

    private static final List<User> users = new ArrayList<>();

    static {
        users.add(new User(UUID.randomUUID(), "olaf", "olaf@example.com"));
        users.add(new User(UUID.randomUUID(), "john", "john@example.com"));
        users.add(new User(UUID.randomUUID(), "anna", "anna@example.com"));
        users.add(new User(UUID.randomUUID(), "mike", "mike@example.com"));
    }

    public List<User> findAll() { return users; }

    public Optional<User> findById(UUID id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst();
    }
}
