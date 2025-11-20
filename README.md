# History API - Jakarta EE with JPA

Aplikacja Historia API z wykorzystaniem Jakarta Persistence (JPA) i bazy danych H2.

## Uruchomienie

```cmd
.\mvnw.cmd clean package -P liberty
.\mvnw.cmd -P liberty liberty:create
.\mvnw.cmd -P liberty liberty:install-feature
.\mvnw.cmd -P liberty liberty:run
```

Aplikacja dostępna pod: http://localhost:9080/History-API/

## Technologie

- Jakarta EE 10
- Jakarta Persistence (JPA) 3.1
- **Jakarta Enterprise Beans (EJB) 4.0** - logika biznesowa i repozytoria
- H2 Database (in-memory)
- Jakarta Faces (JSF) 4.0
- Jakarta RESTful Web Services (JAX-RS) 3.1
- Open Liberty 25.0.0.11

## Architektura EJB

Aplikacja wykorzystuje **Enterprise Java Beans (EJB)** do zarządzania logiką biznesową:

### Warstwa serwisów (@Stateless)
- `HistoricalFigureService`
- `NoteService`
- `UserService`
- `AvatarService`

**Cechy:**
- Bezstanowe beany EJB (`@Stateless`)
- Automatyczne zarządzanie transakcjami
- Pooling i zarządzanie zasobami przez kontener
- Domyślny `TransactionAttributeType.REQUIRED`

### Warstwa repozytoriów (@Stateless)
- `HistoricalFigureRepository`
- `NoteRepository`
- `UserRepository`
- `AvatarRepository`

**Cechy:**
- Bezstanowe beany EJB (`@Stateless`)
- Niezależne od zasięgu HTTP request
- `@PersistenceContext` - EntityManager zarządzany przez kontener
- Thread-safe dzięki request-scoped EntityManager

### Inicjalizacja danych (@Singleton @Startup)
- `DataInitializer` - ładuje dane testowe przy starcie aplikacji
- `@PostConstruct` - metoda inicjalizująca wywoływana automatycznie

### Przepływ architektury:
```
JAX-RS Controllers (stateless)
    ↓ @Inject
EJB Services (@Stateless)
    ↓ @Inject
EJB Repositories (@Stateless)
    ↓ @PersistenceContext
EntityManager (request-scoped)
    ↓
JPA Entities
    ↓
H2 Database
```

## Security & Authorization

Aplikacja wykorzystuje **Jakarta Security API** z **Basic Authentication**:

### Użytkownicy testowi:
- **ADMIN**: `admin / admin123` - pełne uprawnienia
- **USER**: `test / password123` - ograniczone uprawnienia

### Role i uprawnienia:

#### Historical Figures (Kategorie):
- ✅ **GET** - `@PermitAll` - każdy zalogowany użytkownik
- ✅ **POST** - `@RolesAllowed("ADMIN")` - tylko administrator
- ✅ **PUT** - `@RolesAllowed("ADMIN")` - tylko administrator
- ✅ **DELETE** - `@RolesAllowed("ADMIN")` - tylko administrator

#### Notes (Elementy):
- ✅ **GET** (lista) - ADMIN widzi wszystkie, USER tylko swoje (filtrowanie w bazie)
- ✅ **GET** (pojedynczy) - ADMIN widzi wszystkie, USER tylko swoje
- ✅ **POST** - każdy zalogowany może tworzyć, właściciel ustawiany automatycznie
- ✅ **PUT** - ADMIN edytuje wszystkie, USER tylko swoje
- ✅ **DELETE** - ADMIN usuwa wszystkie, USER tylko swoje

#### Users:
- ✅ **POST** (rejestracja) - `@PermitAll` - każdy może się zarejestrować
- ✅ **GET** (lista/pojedynczy) - `@RolesAllowed("ADMIN")` - tylko administrator
- ✅ **DELETE** - `@RolesAllowed("ADMIN")` - tylko administrator

### Automatyczne funkcje:
- 🔐 **Automatyczne ustawienie właściciela** przy tworzeniu notatki
- 🔍 **Filtrowanie na poziomie bazy danych** (JPQL WHERE createdBy = :username)
- 🚫 **HTTP 403 Forbidden** przy próbie dostępu do cudzych zasobów
- 🔑 **HTTP 401 Unauthorized** przy braku autoryzacji

### Konfiguracja:
- `DatabaseIdentityStore` - uwierzytelnianie z bazy danych
- `@BasicAuthenticationMechanismDefinition` - Basic Auth
- `@RolesAllowed` / `@PermitAll` - deklaratywna autoryzacja



## Zrealizowane zadania JPA

1. ✅ Skonfigurowana jednostka trwałości `historyPU` (persistence.xml)
2. ✅ Encje z adnotacjami JPA (@Entity, @Table, @Id, @Column)
3. ✅ Relacja dwukierunkowa OneToMany/ManyToOne między HistoricalFigure i Note
4. ✅ CASCADE REMOVE - automatyczne usuwanie notatek przy usunięciu postaci
5. ✅ LAZY fetch - notatki nie są pobierane automatycznie
6. ✅ EntityManager z @PersistenceContext w repozytoriach
7. ✅ Transakcje zarządzane przez @Transactional
8. ✅ JPQL queries dla wyszukiwania danych
9. ✅ Automatyczna inicjalizacja danych przez @Singleton @Startup

## Struktura bazy danych

### Tabele
- `historical_figures` - postaci historyczne
- `notes` - notatki o postaciach

### Relacje
- Note.historicalFigure → HistoricalFigure (ManyToOne)
- HistoricalFigure.notes → List<Note> (OneToMany)

## Endpointy API

### Użytkownicy
- `GET /api/users` - lista wszystkich użytkowników
- `GET /api/users/{userId}` - pojedynczy użytkownik
- `POST /api/users` - rejestracja nowego użytkownika
- `DELETE /api/users/{userId}` - usunięcie użytkownika

### Avatary użytkowników
- `GET /api/users/{userId}/avatar` - pobranie avatara użytkownika
- `POST /api/users/{userId}/avatar` - upload avatara (JPG/PNG)
  - **Content-Type**: `image/jpeg` lub `image/png`
  - **Body**: raw binary data (nie multipart/form-data)
- `DELETE /api/users/{userId}/avatar` - usunięcie avatara

**Przykład curl**:
```bash
curl -X POST "http://localhost:9080/historyapi/api/users/{userId}/avatar" \
  -H "Content-Type: image/jpeg" \
  --data-binary "@avatar.jpg"
```

Więcej przykładów znajdziesz w pliku `avatar_test_commands.txt`

### Postaci historyczne
- `GET /api/figures` - lista wszystkich postaci
- `GET /api/figures/{id}` - pojedyncza postać
- `POST /api/figures` - utworzenie nowej postaci
- `PUT /api/figures/{id}` - aktualizacja postaci
- `DELETE /api/figures/{id}` - usunięcie postaci (+ wszystkie notatki)

### Notatki (hierarchiczne)
- `GET /api/figures/{figureId}/notes` - wszystkie notatki dla postaci
- `GET /api/figures/{figureId}/notes/{noteId}` - pojedyncza notatka
- `POST /api/figures/{figureId}/notes` - utworzenie notatki
- `PUT /api/figures/{figureId}/notes/{noteId}` - aktualizacja notatki
- `DELETE /api/figures/{figureId}/notes/{noteId}` - usunięcie notatki

### Notatki (bezpośrednie)
- `GET /api/notes` - wszystkie notatki
- `GET /api/notes/{noteId}` - pojedyncza notatka
- `DELETE /api/notes/{noteId}` - usunięcie notatki

Wszystkie przykłady requestów znajdziesz w pliku `requests.http`

## Konfiguracja

### Katalogi przechowywania plików
Katalogi dla avatarów i zdjęć postaci są konfigurowane w `web.xml`:
- Avatary użytkowników: `C:/temp/avatars`
- Zdjęcia postaci: `C:/temp/figure_images`

Katalogi są tworzone automatycznie przy starcie aplikacji.

