package pl.edu.pg.eti.kask.historyapi.user.service;

import pl.edu.pg.eti.kask.historyapi.user.entity.User;
import pl.edu.pg.eti.kask.historyapi.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserService {


    public static List<User> findAll() {
        return UserRepository.findAll();
    }

    public static Optional<User> findById(UUID id) {
        return UserRepository.findById(id);
    }


}
