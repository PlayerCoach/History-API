package pl.edu.pg.eti.kask.historyapi.user.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import pl.edu.pg.eti.kask.historyapi.user.entity.User;
import pl.edu.pg.eti.kask.historyapi.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserService {

    private UserRepository repository;


    public UserService() {}
    @Inject
    public UserService(UserRepository repository) {
        this.repository = repository;
    }


    public  List<User> findAll() {
        return repository.findAll();
    }

    public Optional<User> findById(UUID id) {
        return repository.findById(id);
    }


}
