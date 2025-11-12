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
- H2 Database (in-memory)
- Jakarta Faces (JSF) 4.0
- Jakarta RESTful Web Services (JAX-RS) 3.1
- Open Liberty 25.0.0.11

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

