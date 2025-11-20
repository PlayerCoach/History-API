package pl.edu.pg.eti.kask.historyapi.configuration;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.entity.HistoricalFigure;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.service.HistoricalFigureService;
import pl.edu.pg.eti.kask.historyapi.note.entity.Mode;
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;
import pl.edu.pg.eti.kask.historyapi.note.service.NoteService;
import pl.edu.pg.eti.kask.historyapi.user.entity.Role;
import pl.edu.pg.eti.kask.historyapi.user.entity.User;
import pl.edu.pg.eti.kask.historyapi.user.service.UserService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
@Startup
public class DataInitializer {

    @Inject
    private HistoricalFigureService figureService;

    @Inject
    private NoteService noteService;

    @Inject
    private UserService userService;

    @Inject
    private Pbkdf2PasswordHash passwordHash;

    @PostConstruct
    public void init() {
        // Configure password hash
        Map<String, String> params = new HashMap<>();
        params.put("Pbkdf2PasswordHash.Iterations", "210000");
        params.put("Pbkdf2PasswordHash.Algorithm", "PBKDF2WithHmacSHA256");
        params.put("Pbkdf2PasswordHash.SaltSizeBytes", "32");
        passwordHash.initialize(params);

        // Create Users with hashed passwords
        User admin = new User(UUID.fromString("fe003ce8-0dae-46cb-8d01-104d1d91d4a0"), "admin", "admin@test.test");
        admin.setPassword(passwordHash.generate("admin123".toCharArray()));
        admin.setRole(Role.ADMIN);

        User user1 = new User(UUID.randomUUID(), "test", "test@test.test");
        user1.setPassword(passwordHash.generate("password123".toCharArray()));
        user1.setRole(Role.USER);

        User user2 = new User(UUID.randomUUID(), "olaf", "olaf@example.com");
        user2.setPassword(passwordHash.generate("password123".toCharArray()));
        user2.setRole(Role.USER);

        User user3 = new User(UUID.randomUUID(), "john", "john@example.com");
        user3.setPassword(passwordHash.generate("password123".toCharArray()));
        user3.setRole(Role.USER);

        User user4 = new User(UUID.randomUUID(), "anna", "anna@example.com");
        user4.setPassword(passwordHash.generate("password123".toCharArray()));
        user4.setRole(Role.USER);

        userService.save(admin);
        userService.save(user1);
        userService.save(user2);
        userService.save(user3);
        userService.save(user4);

        // Create Historical Figures
        HistoricalFigure napoleon = new HistoricalFigure();
        napoleon.setId(UUID.fromString("15a7f9a0-7ac1-11eb-8000-0242ac110002"));
        napoleon.setName("Napoleon Bonaparte");
        napoleon.setDateOfBrith(LocalDate.of(1769, 8, 15));
        napoleon.setDateOfDeath(LocalDate.of(1821, 5, 5));

        HistoricalFigure curie = new HistoricalFigure();
        curie.setId(UUID.fromString("15a7fae0-7ac1-11eb-8001-0242ac110002"));
        curie.setName("Maria Sklodowska-Curie");
        curie.setDateOfBrith(LocalDate.of(1867, 11, 7));
        curie.setDateOfDeath(LocalDate.of(1934, 7, 4));

        figureService.save(napoleon);
        figureService.save(curie);

        // Create Notes
        Note n1 = new Note();
        n1.setId(UUID.fromString("25b8c1f0-7ac1-11eb-8000-0242ac110002"));
        n1.setTitle("Bitwa pod Waterloo");
        n1.setContent("Ostateczna porazka Napoleona.");
        n1.setMode(Mode.PUBLIC);
        n1.setHistoricalFigure(napoleon);
        n1.setCreatedBy(admin);

        Note n2 = new Note();
        n2.setId(UUID.fromString("25b8c3a0-7ac1-11eb-8001-0242ac110002"));
        n2.setTitle("Odkrycie Polonu");
        n2.setContent("Pierwiastek nazwany na czesc Polski.");
        n2.setMode(Mode.PUBLIC);
        n2.setHistoricalFigure(curie);
        n2.setCreatedBy(user1);

        Note n3 = new Note();
        n3.setId(UUID.randomUUID());
        n3.setTitle("Odkrycie Radu");
        n3.setContent("Kolejne wielkie odkrycie.");
        n3.setMode(Mode.PRIVATE);
        n3.setHistoricalFigure(curie);
        n3.setCreatedBy(user2);

        noteService.save(n1);
        noteService.save(n2);
        noteService.save(n3);
    }
}
