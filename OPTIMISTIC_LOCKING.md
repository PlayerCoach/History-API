# Optimistic Locking w aplikacji History-API

## Wprowadzenie

**Optimistic Locking** (blokowanie optymistyczne) to mechanizm kontroli współbieżności, który zakłada, że konflikty podczas równoczesnej edycji danych są rzadkie. Zamiast blokować rekordy na czas edycji, system sprawdza w momencie zapisu, czy dane nie zostały zmodyfikowane przez innego użytkownika.

## Przepływ danych w aplikacji

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   note_edit     │     │  NoteEditView   │     │   NoteService   │     │ NoteRepository  │
│    .xhtml       │────▶│    (JSF Bean)   │────▶│   (EJB @Stateless)   │────▶│  (EntityManager)│
│   (Widok)       │     │  (@ViewScoped)  │     │                 │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘     └─────────────────┘
                                │                        │                        │
                                │                        │                        ▼
                                │                        │                  ┌───────────┐
                                │                        │                  │  Baza     │
                                │                        │                  │  Danych   │
                                │                        │                  └───────────┘
                                │                        │
                                ◀────────────────────────┘
                        OptimisticLockException
                        (opakowany w EJBException)
```

## Konfiguracja w encji Note

W klasie `Note.java` pole wersji jest oznaczone adnotacją `@Version`:

```java
@Entity
@Table(name = "notes")
public class Note implements Serializable {

    @Id
    private UUID id;

    @Version
    private Long version;  // <-- Pole wersji dla optimistic locking
    
    // ...pozostałe pola
}
```

### Jak działa pole @Version?

1. **Przy pierwszym zapisie** - JPA automatycznie ustawia `version = 0`
2. **Przy każdym update** - JPA automatycznie inkrementuje wersję (`version++`)
3. **Przy merge/update** - JPA porównuje wersję z formularza z wersją w bazie

## Przepływ przy zapisie (bez konfliktu)

```
1. Użytkownik otwiera formularz edycji
   └── loadData() pobiera notatkę z bazy (version = 5)
   
2. Użytkownik edytuje dane i klika "Zapisz"
   └── save() wywołuje noteService.save(note)
       └── noteService.save(note) wywołuje repository.save(note)
           └── em.merge(note) - JPA sprawdza: version w bazie == 5, version w encji == 5 ✓
               └── UPDATE notes SET ..., version = 6 WHERE id = ? AND version = 5
               
3. Sukces - przekierowanie do strony postaci
```

## Przepływ przy konflikcie wersji

```
UŻYTKOWNIK A                         UŻYTKOWNIK B
     │                                    │
     ├── Otwiera notatkę (v=5)           │
     │                                    ├── Otwiera tę samą notatkę (v=5)
     │                                    │
     ├── Edytuje...                       ├── Edytuje...
     │                                    │
     │                                    ├── Zapisuje pierwszy! (v=5 → v=6) ✓
     │                                    │
     ├── Próbuje zapisać (v=5)           │
     │   └── BŁĄD! Baza ma już v=6       │
     │       └── OptimisticLockException │
     │                                    │
     ├── Widzi komunikat o konflikcie    │
     │   - Dane z bazy (v=6)             │
     │   - Swoje wprowadzone dane        │
     │                                    │
     └── Może wybrać:                    │
         - Przyjąć wersję z bazy         │
         - Ponowić zapis swoich danych   │
```

## Obsługa wyjątku w NoteEditView

### 1. Metoda save() - przechwycenie wyjątku

```java
public String save() {
    try {
        noteService.save(note);
        // Sukces - przekierowanie
        return "/historicalfigure/figure?faces-redirect=true&figureId=" + figureIdForRedirect;
        
    } catch (OptimisticLockException e) {
        // Bezpośredni wyjątek JPA
        handleVersionConflict();
        return null;
        
    } catch (EJBException e) {
        // OptimisticLockException jest OPAKOWANY w EJBException!
        // To jest kluczowe - EJB kontenery opakowują wyjątki
        if (isOptimisticLockException(e)) {
            handleVersionConflict();
            return null;
        }
        throw e;
    }
}
```

### 2. Dlaczego EJBException?

**Ważne:** `NoteService` jest EJB (`@Stateless`), więc kontener EJB opakowuje wyjątki w `EJBException`. Dlatego musimy sprawdzać cały łańcuch przyczyn:

```java
private boolean isOptimisticLockException(Throwable e) {
    Throwable cause = e;
    while (cause != null) {
        if (cause instanceof OptimisticLockException) {
            return true;
        }
        // EclipseLink ma własną klasę OptimisticLockException
        if (cause.getClass().getName().contains("OptimisticLockException")) {
            return true;
        }
        cause = cause.getCause();
    }
    return false;
}
```

### 3. Obsługa konfliktu - handleVersionConflict()

```java
private void handleVersionConflict() {
    versionConflict = true;  // Flaga dla widoku

    // 1. Zachowaj dane wprowadzone przez użytkownika
    userEnteredNote = new Note();
    userEnteredNote.setTitle(note.getTitle());
    userEnteredNote.setContent(note.getContent());
    userEnteredNote.setMode(note.getMode());
    userEnteredNote.setVersion(note.getVersion());

    // 2. Pobierz AKTUALNE dane z bazy
    currentDatabaseNote = noteService.findById(note.getId()).orElse(null);

    // 3. Zaktualizuj wersję w formularzu (dla kolejnej próby zapisu)
    if (currentDatabaseNote != null) {
        note.setVersion(currentDatabaseNote.getVersion());
    }

    // 4. Pokaż komunikat użytkownikowi
    FacesContext.getCurrentInstance().addMessage(null,
        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Konflikt wersji", null));
}
```

### 4. Akcje użytkownika po konflikcie

**Opcja A: Przyjmij wersję z bazy**
```java
public String acceptCurrentVersion() {
    // Nadpisz formularz danymi z bazy
    note.setTitle(currentDatabaseNote.getTitle());
    note.setContent(currentDatabaseNote.getContent());
    note.setMode(currentDatabaseNote.getMode());
    note.setVersion(currentDatabaseNote.getVersion());
    
    // Wyczyść stan konfliktu
    versionConflict = false;
    currentDatabaseNote = null;
    userEnteredNote = null;
    return null;  // Pozostań na stronie
}
```

**Opcja B: Ponów zapis swoich danych**
```java
public String retryWithUserData() {
    // Przywróć dane użytkownika ale z AKTUALNĄ wersją
    note.setTitle(userEnteredNote.getTitle());
    note.setContent(userEnteredNote.getContent());
    note.setMode(userEnteredNote.getMode());
    note.setVersion(currentDatabaseNote.getVersion());  // Klucz!
    
    // Wyczyść stan i spróbuj ponownie
    versionConflict = false;
    currentDatabaseNote = null;
    userEnteredNote = null;
    return save();  // Ponowna próba zapisu
}
```

## Odpowiedź na pytanie: Czy przepływa przez NoteService?

**TAK!** Przepływ jest następujący:

```
NoteEditView.save()
    │
    └── noteService.save(note)     ◀── Wywołanie serwisu
            │
            └── repository.save(note)
                    │
                    └── em.merge(note)  ◀── Tu JPA rzuca OptimisticLockException
                            │
                            └── WYJĄTEK propaguje w górę przez:
                                    │
                                    repository.save() → (wyjątek leci dalej)
                                    │
                                    noteService.save() → EJB OPAKOWUJE w EJBException
                                    │
                                    NoteEditView.save() → ŁAPIE EJBException
                                                         sprawdza przyczynę
                                                         wywołuje handleVersionConflict()
```

### Kluczowe punkty:

1. **Wyjątek powstaje w EntityManager** (`em.merge()`)
2. **Propaguje przez Repository** (nie jest łapany)
3. **Propaguje przez Service** (EJB opakowuje go w `EJBException`)
4. **Jest łapany w View** (`NoteEditView.save()`)

## Dlaczego NoteService nie łapie wyjątku?

Service (`NoteService`) jest warstwą logiki biznesowej. Nie wie i nie powinien wiedzieć, jak obsłużyć konflikt wersji w kontekście UI. To zadanie warstwy prezentacji (`NoteEditView`), która:

- Może pokazać komunikat użytkownikowi
- Ma dostęp do `FacesContext`
- Może przechować dane użytkownika do wyświetlenia
- Może oferować akcje (przyjmij/ponów)

## Widok (XHTML) - wyświetlanie konfliktu

W pliku `note_edit.xhtml` powinien być panel wyświetlany przy konflikcie:

```xhtml
<h:panelGroup rendered="#{noteEditView.versionConflict}">
    <h:outputText value="Konflikt wersji! Dane zostały zmienione przez innego użytkownika." 
                  styleClass="error"/>
    
    <h:panelGrid columns="3">
        <h:outputText value="Pole"/>
        <h:outputText value="Twoje dane"/>
        <h:outputText value="Dane z bazy"/>
        
        <h:outputText value="Tytuł:"/>
        <h:outputText value="#{noteEditView.userEnteredNote.title}"/>
        <h:outputText value="#{noteEditView.currentDatabaseNote.title}"/>
        
        <h:outputText value="Treść:"/>
        <h:outputText value="#{noteEditView.userEnteredNote.content}"/>
        <h:outputText value="#{noteEditView.currentDatabaseNote.content}"/>
    </h:panelGrid>
    
    <h:commandButton value="Przyjmij wersję z bazy" 
                     action="#{noteEditView.acceptCurrentVersion}"/>
    <h:commandButton value="Zapisz moje dane" 
                     action="#{noteEditView.retryWithUserData}"/>
</h:panelGroup>
```

## Podsumowanie

| Warstwa | Plik | Rola |
|---------|------|------|
| **Encja** | `Note.java` | `@Version` - pole wersji |
| **Repository** | `NoteRepository.java` | `em.merge()` - tu powstaje wyjątek |
| **Service** | `NoteService.java` | Przepuszcza wyjątek (EJB opakowuje) |
| **View Bean** | `NoteEditView.java` | Łapie i obsługuje wyjątek |
| **XHTML** | `note_edit.xhtml` | Wyświetla konflikt użytkownikowi |

