# Przykładowe dane użytkowników do testowania autoryzacji

## Ważne:
Hasła muszą być zahashowane algorytmem PBKDF2WithHmacSHA256 z parametrami:
- Iterations: 210000
- Algorithm: PBKDF2WithHmacSHA256  
- SaltSizeBytes: 32

## Opcja 1: Użycie UserService.save()

Możesz utworzyć użytkowników programowo - UserService automatycznie zahashuje hasła:

```java
User admin = new User();
admin.setId(UUID.randomUUID());
admin.setLogin("admin");
admin.setEmail("admin@example.com");
admin.setPassword("admin"); // Zostanie zahashowane przez UserService
admin.setRole(Role.ADMIN);
userService.save(admin);

User user = new User();
user.setId(UUID.randomUUID());
user.setLogin("user");
user.setEmail("user@example.com");
user.setPassword("user"); // Zostanie zahashowane przez UserService
user.setRole(Role.USER);
userService.save(user);
```

## Opcja 2: Utworzenie Data Loader

Możesz utworzyć klasę InitialDataLoader z @Startup @Singleton:

```java
package pl.edu.pg.eti.kask.historyapi.configuration;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import pl.edu.pg.eti.kask.historyapi.user.entity.Role;
import pl.edu.pg.eti.kask.historyapi.user.entity.User;
import pl.edu.pg.eti.kask.historyapi.user.service.UserService;

import java.util.UUID;

@Singleton
@Startup
public class InitialDataLoader {

    @Inject
    private UserService userService;

    @PostConstruct
    public void init() {
        // Sprawdź czy admin już istnieje
        if (userService.findByLogin("admin").isEmpty()) {
            User admin = new User();
            admin.setId(UUID.randomUUID());
            admin.setLogin("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword("admin");
            admin.setRole(Role.ADMIN);
            userService.save(admin);
            System.out.println("Created admin user: admin/admin");
        }

        if (userService.findByLogin("user").isEmpty()) {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setLogin("user");
            user.setEmail("user@example.com");
            user.setPassword("user");
            user.setRole(Role.USER);
            userService.save(user);
            System.out.println("Created regular user: user/user");
        }
    }
}
```

## Testowe dane logowania:

Po utworzeniu użytkowników powyższymi metodami, możesz się zalogować:

### Administrator:
- Login: `admin`
- Hasło: `admin`
- Rola: `ADMIN`

### Zwykły użytkownik:
- Login: `user`
- Hasło: `user`
- Rola: `USER`

## Testowanie w REST API (Basic Auth):

### Nagłówek Authorization dla admina:
```
Authorization: Basic YWRtaW46YWRtaW4=
```
(base64 encode "admin:admin")

### Nagłówek Authorization dla usera:
```
Authorization: Basic dXNlcjp1c2Vy
```
(base64 encode "user:user")

## Przykładowe zapytania HTTP:

```http
### Login as admin
GET http://localhost:9080/History-API/api/notes
Authorization: Basic YWRtaW46YWRtaW4=

### Login as user
GET http://localhost:9080/History-API/api/notes
Authorization: Basic dXNlcjp1c2Vy

### Get specific note (admin)
GET http://localhost:9080/History-API/api/notes/{{noteId}}
Authorization: Basic YWRtaW46YWRtaW4=
```

