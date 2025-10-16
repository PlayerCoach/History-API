package pl.edu.pg.eti.kask.historyapi.user.repository;

import pl.edu.pg.eti.kask.historyapi.user.entity.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserRepository {


    private static final Map<UUID, User> users = new ConcurrentHashMap<>();

    static {

        User user1 = new User(UUID.fromString("fe003ce8-0dae-46cb-8d01-104d1d91d4a0"), "test", "test@test.test");
        users.put(user1.getId(), user1); // Predefined user with fixed UUID for testing purposes

        User user2 = new User(UUID.randomUUID(), "olaf", "olaf@example.com");
        users.put(user2.getId(), user2);

        User user3 = new User(UUID.randomUUID(), "john", "john@example.com");
        users.put(user3.getId(), user3);

        User user4 = new User(UUID.randomUUID(), "anna", "anna@example.com");
        users.put(user4.getId(), user4);
    }


    public static List<User> findAll() {
        return new ArrayList<>(users.values());
    }


    public static Optional<User> findById(UUID id) {
        return Optional.ofNullable(users.get(id));
    }
}