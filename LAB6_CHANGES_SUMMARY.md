# Lab 6 - Podsumowanie Zmian

## 🎯 Zrealizowane Zadania

### ✅ Zadanie 1: Obsługa użytkowników w systemie (1.0 punkt)
- **Związek User ↔ Note**: Jednokierunkowy po stronie `Note` (`@ManyToOne createdBy`)
- **Dane testowe**: `DataInitializer` tworzy 5 użytkowników (admin + 4 userów) z hashowanymi hasłami
- **Rejestracja REST**: Endpoint `POST /api/users` z adnotacją `@PermitAll`

### ✅ Zadanie 2: Beany EJB (2.0 punkty)
- **Serwisy jako EJB**: Wszystkie serwisy (`HistoricalFigureService`, `NoteService`, `UserService`) mają adnotację `@Stateless`
- **Repozytoria niezależne od HTTP**: Wszystkie repozytoria (`HistoricalFigureRepository`, `NoteRepository`, `UserRepository`) mają adnotację `@Stateless` (nie są scopowane do request)

### ✅ Zadanie 3: Domena bezpieczeństwa (1.0 punkt)
- **Basic Authentication**: `@BasicAuthenticationMechanismDefinition` w `SecurityConfig`
- **Użytkownicy z bazy**: `@DatabaseIdentityStoreDefinition` z zapytaniami SQL:
  - `callerQuery`: SELECT password FROM users WHERE login = ?
  - `groupsQuery`: SELECT role FROM users WHERE login = ?
- **Hashowanie PBKDF2**: `Pbkdf2PasswordHash` z parametrami (210000 iteracji, PBKDF2WithHmacSHA256, 32-byte salt)

### ✅ Zadanie 4: Autoryzacja (2.0 punkty)

#### Historical Figures (kategorie):
- ✅ Tylko ADMIN może dodawać (`POST @RolesAllowed("ADMIN")`)
- ✅ Tylko ADMIN może usuwać (`DELETE @RolesAllowed("ADMIN")`)
- ✅ Zalogowani użytkownicy (ADMIN i USER) mogą pobierać (`GET @RolesAllowed({"ADMIN", "USER"})`)

#### Notes (elementy):
- ✅ ADMIN może pobrać wszystkie elementy
- ✅ USER może pobrać tylko swoje (filtrowanie **na poziomie bazy danych** przez `findByOwner()`)
- ✅ ADMIN może edytować i usuwać wszystkie
- ✅ USER może edytować i usuwać tylko swoje (sprawdzanie w kontrolerze)
- ✅ USER może dodawać (właściciel ustawiany automatycznie w kontrolerze)

#### Users:
- ✅ Każdy może się zarejestrować (`POST @PermitAll`)
- ✅ Tylko ADMIN może pobierać listę użytkowników (`GET @RolesAllowed("ADMIN")`)
- ✅ Tylko ADMIN może usuwać użytkowników (`DELETE @RolesAllowed("ADMIN")`)

---

## 📝 Zmodyfikowane Pliki

### 1. **SecurityConfig.java**
```java
@ApplicationScoped
@BasicAuthenticationMechanismDefinition(realmName = "history-api-realm")
@DatabaseIdentityStoreDefinition(
    dataSourceLookup = "jdbc/historyDS",
    callerQuery = "SELECT password FROM users WHERE login = ?",
    groupsQuery = "SELECT role FROM users WHERE login = ?",
    hashAlgorithm = Pbkdf2PasswordHash.class,
    hashAlgorithmParameters = { ... }
)
```

### 2. **web.xml**
Dodano deklaracje ról:
```xml
<security-role>
    <role-name>ADMIN</role-name>
</security-role>
<security-role>
    <role-name>USER</role-name>
</security-role>
```

### 3. **ApplicationConfig.java**
Dodano:
```java
@DeclareRoles({"ADMIN", "USER"})
```

### 4. **server.xml**
Dodano konfigurację HTTP endpoint:
```xml
<httpEndpoint id="defaultHttpEndpoint"
              host="*"
              httpPort="9080"
              httpsPort="9443" />
```

### 5. **NoteRepository.java**
Dodano metodę do filtrowania na poziomie bazy:
```java
public List<Note> findByOwner(String username) {
    return em.createQuery("SELECT n FROM Note n WHERE n.createdBy.login = :username", Note.class)
            .setParameter("username", username)
            .getResultList();
}
```

### 6. **NoteService.java**
Dodano delegację do repository:
```java
public List<Note> findByOwner(String username) {
    return repository.findByOwner(username);
}
```

### 7. **NoteSimpleController.java**
Zmieniono filtrowanie z aplikacyjnego na bazodanowe:
```java
if (isAdmin) {
    notes = noteService.findAll();
} else {
    notes = noteService.findByOwner(username); // ← DB-level filtering
}
```

### 8. **Nowe pliki testowe**
- `requests_lab6_tests.http` - kompletny zestaw testów funkcjonalnych
- `requests_lab6_auth.http` - testy bezpieczeństwa i autoryzacji

---

## 🚀 Jak uruchomić i przetestować

### 1. Wyczyść projekt i zrestartuj serwer
```powershell
mvn clean
```
Potem zatrzymaj i uruchom ponownie serwer Liberty z UI IntelliJ.

### 2. Domyślni użytkownicy testowi
Po uruchomieniu aplikacji w bazie będą:

| Login  | Hasło        | Rola  |
|--------|--------------|-------|
| admin  | admin123     | ADMIN |
| test   | password123  | USER  |
| olaf   | password123  | USER  |
| john   | password123  | USER  |
| anna   | password123  | USER  |

### 3. Uruchom testy HTTP
Otwórz w IntelliJ:
- `requests.http` (pełny zestaw testów)
- `requests_lab6_tests.http` (testy Lab 6)
- `requests_lab6_auth.http` (testy autoryzacji)

Kliknij zielony przycisk "Run" przy każdym teście.

### 4. Format autoryzacji
W IntelliJ HTTP Client używaj:
```http
Authorization: Basic username:password
```
IntelliJ automatycznie zakoduje to w Base64.

---

## ✅ Checklist wymagań Lab 6

- [x] Związek User ↔ Note (jednokierunkowy)
- [x] Dane testowe z użytkownikami
- [x] Rejestracja przez REST (`@PermitAll`)
- [x] Serwisy jako `@Stateless` EJB
- [x] Repozytoria niezależne od HTTP request scope
- [x] Basic Authentication
- [x] DatabaseIdentityStore z hasłami z bazy
- [x] Hashowanie PBKDF2
- [x] ADMIN: dodawanie/usuwanie kategorii
- [x] USER: pobieranie kategorii
- [x] Każdy: rejestracja
- [x] ADMIN: pobieranie wszystkich elementów
- [x] USER: pobieranie tylko swoich (filtrowanie DB)
- [x] ADMIN: edycja/usuwanie wszystkich elementów
- [x] USER: edycja/usuwanie tylko swoich
- [x] USER: dodawanie elementów (właściciel auto)

---

## 🐛 Rozwiązane problemy

1. **StringIndexOutOfBoundsException** - dodano `hashAlgorithm` w `@DatabaseIdentityStoreDefinition`
2. **Brak deklaracji ról** - dodano `@DeclareRoles` i `<security-role>` w web.xml
3. **Filtrowanie w aplikacji** - zmieniono na filtrowanie bazodanowe (`findByOwner`)
4. **Format Basic Auth** - poprawiono w plikach `.http`

---

## 📊 Struktura Autoryzacji

```
HistoricalFigureController:
  GET /figures              → @RolesAllowed({"ADMIN", "USER"})
  GET /figures/{id}         → @RolesAllowed({"ADMIN", "USER"})
  POST /figures             → @RolesAllowed("ADMIN")
  PUT /figures/{id}         → @RolesAllowed("ADMIN")
  DELETE /figures/{id}      → @RolesAllowed("ADMIN")

NoteController (hierarchiczny):
  GET /figures/{id}/notes                    → @RolesAllowed({"ADMIN", "USER"})
  GET /figures/{id}/notes/{noteId}           → @RolesAllowed({"ADMIN", "USER"}) + ownership check
  POST /figures/{id}/notes                   → @RolesAllowed({"ADMIN", "USER"}) + auto set owner
  PUT /figures/{id}/notes/{noteId}           → @RolesAllowed({"ADMIN", "USER"}) + ownership check
  DELETE /figures/{id}/notes/{noteId}        → @RolesAllowed({"ADMIN", "USER"}) + ownership check

NoteSimpleController (prosty):
  GET /notes                 → @RolesAllowed({"ADMIN", "USER"}) + DB filtering
  GET /notes/{noteId}        → @RolesAllowed({"ADMIN", "USER"}) + ownership check
  DELETE /notes/{noteId}     → @RolesAllowed({"ADMIN", "USER"}) + ownership check

UserController:
  POST /users                → @PermitAll (rejestracja)
  GET /users                 → @RolesAllowed("ADMIN")
  GET /users/{id}            → @RolesAllowed("ADMIN")
  DELETE /users/{id}         → @RolesAllowed("ADMIN")
  GET /users/{id}/avatar     → @RolesAllowed({"ADMIN", "USER"})
  POST /users/{id}/avatar    → @RolesAllowed({"ADMIN", "USER"})
  DELETE /users/{id}/avatar  → @RolesAllowed({"ADMIN", "USER"})
```

---

## 🎓 Punktacja

| Zadanie | Punkty | Status |
|---------|--------|--------|
| 1. Obsługa użytkowników | 1.0 | ✅ |
| 2. Beany EJB | 2.0 | ✅ |
| 3. Domena bezpieczeństwa | 1.0 | ✅ |
| 4. Autoryzacja | 2.0 | ✅ |
| **RAZEM** | **6.0** | ✅ |

---

**Autor:** GitHub Copilot  
**Data:** 2025-11-19

