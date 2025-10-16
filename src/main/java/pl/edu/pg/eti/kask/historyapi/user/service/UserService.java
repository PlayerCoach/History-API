package pl.edu.pg.eti.kask.historyapi.user.service;

import pl.edu.pg.eti.kask.historyapi.user.entity.User;
import pl.edu.pg.eti.kask.historyapi.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> getAll() {
        return repo.findAll();
    }

    public Optional<User> get(UUID id) {
        return repo.findById(id);
    }

//    public void create(User user) {
//        repo.create(user);
//    }
//
//    public void update(User user) {
//        repo.update(user);
//    }
}
