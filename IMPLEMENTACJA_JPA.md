# Implementacja JPA z bazą H2 - Podsumowanie zmian

## Zrealizowane zadania

### 1. Konfiguracja jednostki trwałości (1 punkt)

✅ **Utworzono plik persistence.xml** (`src/main/resources/META-INF/persistence.xml`)
- Skonfigurowana jednostka trwałości `historyPU` z typem transakcji JTA
- Połączenie z bazą danych poprzez JNDI: `jdbc/historyDS`
- Hibernate skonfigurowany do automatycznego tworzenia schematu (`create-drop`)
- Zarejestrowane klasy encyjne: `HistoricalFigure` i `Note`

✅ **Skonfigurowano DataSource w server.xml**
- Dodano feature: `persistence-3.1`, `jdbc-4.3`, `enterpriseBeansLite-4.0`
- Utworzono DataSource dla H2: `jdbc/historyDS`
- Baza danych H2 w pamięci: `memory:historydb`

### 2. Konfiguracja klas encyjnych (1 punkt)

✅ **HistoricalFigure** - adnotacje JPA:
- `@Entity` - oznaczenie jako klasa encji
- `@Table(name = "historical_figures")` - mapowanie na tabelę
- `@Id` - klucz główny (UUID)
- `@Column` - mapowanie kolumn z odpowiednimi nazwami
- `@OneToMany` - relacja do notatek:
  - `mappedBy = "historicalFigure"` - dwukierunkowa
  - `cascade = CascadeType.REMOVE` - automatyczne usuwanie
  - `fetch = FetchType.LAZY` - leniwe ładowanie (nie pobiera automatycznie)
  - `orphanRemoval = true` - usuwanie osieroconych encji
- `@JsonIgnore` - unikanie cyklicznych referencji w JSON

✅ **Note** - adnotacje JPA:
- `@Entity` - oznaczenie jako klasa encji
- `@Table(name = "notes")` - mapowanie na tabelę
- `@Id` - klucz główny (UUID)
- `@Column` - mapowanie kolumn
- `@Enumerated(EnumType.STRING)` - mapowanie enum jako string
- `@ManyToOne` - relacja do HistoricalFigure:
  - `fetch = FetchType.LAZY` - leniwe ładowanie
  - `@JoinColumn(name = "historical_figure_id")` - kolumna klucza obcego
- `@JsonIgnore` - unikanie cyklicznych referencji
- Metody pomocnicze dla JSON: `getHistoricalFigureId()`, `setHistoricalFigureId()`

### 3. Implementacja dostępu do danych przez EntityManager (4 punkty)

✅ **HistoricalFigureRepository**:
- Usunięto `ConcurrentHashMap` i metodę `@PostConstruct`
- Dodano `@PersistenceContext(unitName = "historyPU")` EntityManager
- Zaimplementowano metody:
  - `findAll()` - JPQL query
  - `findById()` - `em.find()`
  - `delete()` - `em.remove()`
  - `save()` - `em.persist()` lub `em.merge()`

✅ **NoteRepository**:
- Usunięto `ConcurrentHashMap` i metodę `@PostConstruct`
- Dodano `@PersistenceContext(unitName = "historyPU")` EntityManager
- Zaimplementowano metody:
  - `findAll()` - JPQL query
  - `findById()` - `em.find()`
  - `findByFigureId()` - JPQL query z parametrem
  - `findByUserId()` - JPQL query z parametrem
  - `delete()` - `em.remove()`
  - `deleteNotesWithHistoricalFigureId()` - JPQL DELETE
  - `save()` - `em.persist()` lub `em.merge()`

✅ **Dodano @Transactional do serwisów**:
- `HistoricalFigureService`: metody `save()` i `delete()`
- `NoteService`: metody `save()`, `delete()`, `deleteNotesWithHistoricalFigureId()`

✅ **Utworzono DataInitializer**:
- Komponent `@Singleton @Startup` do inicjalizacji danych
- Automatyczne tworzenie przykładowych danych przy starcie aplikacji
- Zastępuje poprzednie metody `@PostConstruct` w repozytoriach

### 4. Aktualizacja kontrolerów i widoków

✅ **NoteController** (JAX-RS):
- Zaktualizowano do używania relacji JPA zamiast ID
- Dodano wstrzykiwanie `HistoricalFigureService`
- Metody `createNoteForFigure()` i `updateNote()` ustawiają obiekt HistoricalFigure

✅ **NoteSingularView** (JSF):
- Metoda `getFigure()` pobiera obiekt z relacji JPA

✅ **NoteEditView** (JSF):
- Metoda `loadData()` ustawia obiekt HistoricalFigure zamiast ID
- Metoda `save()` pobiera ID z obiektu relacji

✅ **note_edit.xhtml**:
- Zaktualizowano `h:selectOneMenu` do bindowania obiektu HistoricalFigure
- Dodano własny konwerter `historicalFigureConverter`

✅ **HistoricalFigureConverter**:
- Konwerter JSF dla obiektów HistoricalFigure
- Konwersja UUID ↔ HistoricalFigure

### 5. Dodatkowe zmiany

✅ **pom.xml**:
- Dodano zależność `h2` w wersji 2.2.224
- Zaktualizowano Lombok do wersji 1.18.30 (kompatybilność z Java 17)

✅ **server.xml**:
- Skonfigurowano DataSource z połączeniem do H2 w pamięci
- URL bazy danych: `jdbc:h2:mem:historydb`
- H2 driver jest ładowany automatycznie z `WEB-INF/lib` aplikacji WAR
- Brak konieczności dodatkowej konfiguracji - wszystko działa "out of the box"

## Jak uruchomić

**UWAGA: Aplikacja jest skonfigurowana do automatycznego uruchamiania z Liberty dev mode!**

### Szybkie uruchomienie (zalecane):

```cmd
.\mvnw.cmd clean package -P liberty
.\mvnw.cmd liberty:dev -P liberty
```

To wszystko! Maven automatycznie:
1. Skompiluje projekt
2. Skopiuje bibliotekę H2 do odpowiedniego katalogu
3. Uruchomi Liberty server w trybie deweloperskim
4. Zainicjalizuje bazę danych z przykładowymi danymi

### Alternatywnie - krok po kroku:

1. **Skompilować projekt**:
   ```cmd
   .\mvnw.cmd clean package -P liberty
   ```

2. **Uruchomić serwer**:
   ```cmd
   .\mvnw.cmd liberty:dev -P liberty
   ```

3. **Aplikacja dostępna pod**:
   - http://localhost:9080/History-API/

### Zatrzymanie serwera:

Naciśnij `Ctrl+C` lub wpisz `q` i Enter w terminalu.

## Weryfikacja działania

### Funkcjonalności, które powinny działać:

1. **JSF (widoki)** ✅:
   - Lista postaci historycznych: `/historicalfigure/figures.xhtml`
   - Szczegóły postaci: `/historicalfigure/figure.xhtml`
   - Lista notatek: `/note/notes.xhtml`
   - Edycja/tworzenie notatek: `/note/note_edit.xhtml`

2. **JAX-RS (API REST)** ✅:
   - `GET /api/figures` - lista postaci
   - `GET /api/figures/{id}` - szczegóły postaci
   - `POST /api/figures` - dodanie postaci
   - `PUT /api/figures/{id}` - aktualizacja postaci
   - `DELETE /api/figures/{id}` - usunięcie postaci (wraz z notatkami - CASCADE)
   - `GET /api/figures/{figureId}/notes` - notatki dla postaci
   - `GET /api/notes` - wszystkie notatki
   - `POST /api/figures/{figureId}/notes` - dodanie notatki
   - `PUT /api/figures/{figureId}/notes/{noteId}` - aktualizacja notatki
   - `DELETE /api/notes/{id}` - usunięcie notatki

3. **Relacje JPA** ✅:
   - Dwukierunkowa relacja między HistoricalFigure ↔ Note
   - Automatyczne usuwanie notatek przy usunięciu postaci (CASCADE REMOVE)
   - Leniwe ładowanie (LAZY) - notatki nie są pobierane automatycznie

4. **Transakcje** ✅:
   - Wszystkie operacje modyfikujące dane są transakcyjne
   - Automatyczne zarządzanie transakcjami przez JTA

## Różnice względem poprzedniej implementacji

| Aspekt | Przed (In-Memory) | Po (JPA + H2) |
|--------|-------------------|---------------|
| Przechowywanie | ConcurrentHashMap | Baza H2 w pamięci |
| Inicjalizacja | @PostConstruct w Repository | @Singleton @Startup DataInitializer |
| Dostęp do danych | Bezpośrednie operacje na mapie | EntityManager (JPA) |
| Relacje | UUID (historicalFigureId) | Obiekt HistoricalFigure (ManyToOne) |
| Transakcje | Brak | @Transactional (JTA) |
| Persistence | Utrata danych po restarcie | Utrata danych po restarcie (H2 in-memory) |

## Uwagi

- Baza H2 jest w pamięci (`memory:historydb`), więc dane są tracone po restarcie serwera
- Hibernate używa strategii `create-drop`, czyli schemat jest tworzony przy starcie i usuwany przy zatrzymaniu
- Związek User → Note został zachowany jako UUID (zgodnie z instrukcją: "pomijane są powiązania elementu z użytkownikiem")
- Wszystkie dotychczasowe funkcjonalności (JSF i JAX-RS) działają poprawnie

