package pl.edu.pg.eti.kask.historyapi.user.service;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import pl.edu.pg.eti.kask.historyapi.user.entity.User;
import pl.edu.pg.eti.kask.historyapi.user.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Stateless
@LocalBean
public class UserService {

    private UserRepository repository;

    @Inject
    private Pbkdf2PasswordHash passwordHash;

    public UserService() {}

    @Inject
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public Optional<User> findById(UUID id) {
        return repository.findById(id);
    }

    public Optional<User> findByLogin(String login) {
        return repository.findByLogin(login);
    }

    public void save(User user) {
        // Hash the password before saving if it's not already hashed
        if (user.getPassword() != null && !user.getPassword().startsWith("PBKDF2WithHmacSHA256:")) {
            Map<String, String> params = new HashMap<>();
            params.put("Pbkdf2PasswordHash.Iterations", "210000");
            params.put("Pbkdf2PasswordHash.Algorithm", "PBKDF2WithHmacSHA256");
            params.put("Pbkdf2PasswordHash.SaltSizeBytes", "32");
            passwordHash.initialize(params);

            user.setPassword(passwordHash.generate(user.getPassword().toCharArray()));
        }
        repository.save(user);
    }

    public void delete(UUID id) {
        repository.delete(id);
    }
}
