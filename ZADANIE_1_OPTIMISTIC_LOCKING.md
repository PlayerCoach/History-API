# Zadanie 1: Kontrola wersji dla klasy encyjnej elementu (Optimistic Locking)

## Wymagania
- Skonfigurować mechanizm optimistic locking dla klasy encyjnej `Note`
- Numer wersji wyświetlany na liście elementów
- Poprawna obsługa konfliktu wersji po stronie widoku
- Użytkownik powiadamiony o konflikcie
- Dostęp do aktualnego stanu z bazy i danych wprowadzonych przez użytkownika

**Punktacja:** 0.5 + 0.5 pkt

---

## Implementacja

### 1. Konfiguracja w encji Note

**Plik:** `Note.java`

```java
@Entity
@Table(name = "notes")
public class Note implements Serializable {

    @Id
    private UUID id;

    @Version  // ← Adnotacja JPA dla optimistic locking
    private Long version;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    
    // ...pozostałe pola
}
```

**Co robi `@Version`:**
- JPA automatycznie zarządza tym polem
- Przy pierwszym zapisie: `version = 0`
- Przy każdym update: `version++`
- Przy merge/update JPA sprawdza: czy `version` w bazie == `version` w encji
- Jeśli nie - rzuca `OptimisticLockException`

---

### 2. Wyświetlanie wersji na liście notatek

**Plik:** `notes.xhtml`

```xhtml
<h:dataTable value="#{noteListView.notes}" var="note" styleClass="table">
    <h:column>
        <f:facet name="header">#{msg['notes.column.version']}</f:facet>
        <h:outputText value="#{note.version}" />
    </h:column>
    <!-- ...pozostałe kolumny -->
</h:dataTable>
```

**Bundle messages:**
```properties
# messages_pl.properties
notes.column.version=Wersja

# messages_en.properties
notes.column.version=Version
```

---

### 3. Obsługa OptimisticLockException w NoteEditView

**Plik:** `NoteEditView.java`

#### 3.1. Pola dla obsługi konfliktu

```java
@Getter
private Note note;

// Aktualna wersja z bazy (pokazywana przy konflikcie)
@Getter
private Note currentDatabaseNote;

// Dane wprowadzone przez użytkownika (pokazywane przy konflikcie)
@Getter
private Note userEnteredNote;

// Flaga oznaczająca wystąpienie konfliktu wersji
@Getter
private boolean versionConflict = false;
```

#### 3.2. Metoda save() - przechwycenie wyjątku

```java
public String save() {
    if (noteId != null) {
        checkAccess();
    }

    if (note.getId() == null) {
        note.setId(UUID.randomUUID());
    }

    try {
        noteService.save(note);
        UUID figureIdForRedirect = (note.getHistoricalFigure() != null) 
            ? note.getHistoricalFigure().getId() 
            : null;
        return "/historicalfigure/figure?faces-redirect=true&figureId=" + figureIdForRedirect;
        
    } catch (OptimisticLockException e) {
        handleVersionConflict();
        return null;
        
    } catch (EJBException e) {
        // OptimisticLockException jest OPAKOWANY w EJBException
        // bo NoteService ma @Stateless
        if (isOptimisticLockException(e)) {
            handleVersionConflict();
            return null;
        }
        throw e;
    }
}
```

#### 3.3. Sprawdzanie łańcucha przyczyn wyjątku

```java
private boolean isOptimisticLockException(Throwable e) {
    Throwable cause = e;
    while (cause != null) {
        if (cause instanceof OptimisticLockException) {
            return true;
        }
        // EclipseLink używa własnej klasy OptimisticLockException
        if (cause.getClass().getName().contains("OptimisticLockException")) {
            return true;
        }
        cause = cause.getCause();
    }
    return false;
}
```

#### 3.4. Obsługa konfliktu wersji

```java
private void handleVersionConflict() {
    versionConflict = true;

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

#### 3.5. Akcje użytkownika - przyjęcie wersji z bazy

```java
public String acceptCurrentVersion() {
    if (currentDatabaseNote != null) {
        note.setTitle(currentDatabaseNote.getTitle());
        note.setContent(currentDatabaseNote.getContent());
        note.setMode(currentDatabaseNote.getMode());
        note.setVersion(currentDatabaseNote.getVersion());
    }
    versionConflict = false;
    currentDatabaseNote = null;
    userEnteredNote = null;
    return null;  // Pozostań na stronie
}
```

#### 3.6. Akcje użytkownika - ponowienie zapisu swoich danych

```java
public String retryWithUserData() {
    if (userEnteredNote != null && currentDatabaseNote != null) {
        note.setTitle(userEnteredNote.getTitle());
        note.setContent(userEnteredNote.getContent());
        note.setMode(userEnteredNote.getMode());
        note.setVersion(currentDatabaseNote.getVersion());  // AKTUALNA wersja!
    }
    versionConflict = false;
    currentDatabaseNote = null;
    userEnteredNote = null;
    return save();  // Ponowna próba zapisu
}
```

---

### 4. Widok - wyświetlanie konfliktu

**Plik:** `note_edit.xhtml`

#### 4.1. Ukryte pole wersji w formularzu

```xhtml
<h:form id="noteForm">
    <!-- Ukryte pola dla optimistic locking -->
    <h:inputHidden value="#{noteEditView.note.id}" />
    <h:inputHidden value="#{noteEditView.note.version}" />
    
    <!-- ...pola formularza -->
</h:form>
```

#### 4.2. Panel konfliktu wersji

```xhtml
<h:panelGroup rendered="#{noteEditView.versionConflict}">
  <div class="alert alert-danger mb-4" role="alert">
    <h4 class="alert-heading">#{msg['notes.edit.conflict']}</h4>
    <hr />
    <div class="row">
      <!-- Kolumna 1: Aktualne dane z bazy -->
      <div class="col-md-6">
        <h5>#{msg['notes.edit.conflict.current']}</h5>
        <p>
          <strong>#{msg['notes.edit.field.title']}:</strong>
          #{noteEditView.currentDatabaseNote.title}
        </p>
        <p>
          <strong>#{msg['notes.edit.field.content']}:</strong>
          #{noteEditView.currentDatabaseNote.content}
        </p>
        <p>
          <strong>#{msg['notes.edit.field.mode']}:</strong>
          #{noteEditView.currentDatabaseNote.mode}
        </p>
        <p>
          <strong>#{msg['notes.column.version']}:</strong>
          #{noteEditView.currentDatabaseNote.version}
        </p>
      </div>
      
      <!-- Kolumna 2: Dane wprowadzone przez użytkownika -->
      <div class="col-md-6">
        <h5>#{msg['notes.edit.conflict.your']}</h5>
        <p>
          <strong>#{msg['notes.edit.field.title']}:</strong>
          #{noteEditView.userEnteredNote.title}
        </p>
        <p>
          <strong>#{msg['notes.edit.field.content']}:</strong>
          #{noteEditView.userEnteredNote.content}
        </p>
        <p>
          <strong>#{msg['notes.edit.field.mode']}:</strong>
          #{noteEditView.userEnteredNote.mode}
        </p>
        <p>
          <strong>#{msg['notes.column.version']}:</strong>
          #{noteEditView.userEnteredNote.version}
        </p>
      </div>
    </div>
    <hr />
    
    <!-- Przyciski akcji -->
    <h:form styleClass="d-inline">
      <h:commandButton
        value="#{msg['notes.edit.conflict.acceptCurrent']}"
        action="#{noteEditView.acceptCurrentVersion}"
        styleClass="btn btn-secondary me-2"
      />
      <h:commandButton
        value="#{msg['notes.edit.conflict.retryMine']}"
        action="#{noteEditView.retryWithUserData}"
        styleClass="btn btn-primary"
      />
    </h:form>
  </div>
</h:panelGroup>
```

---

## Bundle Messages

**messages_pl.properties:**
```properties
notes.column.version=Wersja
notes.edit.conflict=Konflikt wersji!
notes.edit.conflict.description=Dane zostały zmienione przez innego użytkownika.
notes.edit.conflict.current=Aktualne dane z bazy
notes.edit.conflict.your=Twoje wprowadzone dane
notes.edit.conflict.acceptCurrent=Przyjmij wersję z bazy
notes.edit.conflict.retryMine=Zapisz moje dane
```

**messages_en.properties:**
```properties
notes.column.version=Version
notes.edit.conflict=Version Conflict!
notes.edit.conflict.description=Data has been modified by another user.
notes.edit.conflict.current=Current data from database
notes.edit.conflict.your=Your entered data
notes.edit.conflict.acceptCurrent=Accept current version
notes.edit.conflict.retryMine=Save my data
```

---

## Przepływ danych

```
User A                          User B
  │                               │
  ├── Otwiera notatkę (v=5)      │
  │                               ├── Otwiera tę samą notatkę (v=5)
  │                               │
  ├── Edytuje...                  ├── Edytuje...
  │                               │
  │                               ├── Zapisuje pierwszy! (v=5 → v=6) ✓
  │                               │
  ├── Próbuje zapisać (v=5)      │
  │   └── BŁĄD! Baza ma już v=6  │
  │       └── OptimisticLockException
  │                               │
  ├── Widzi panel konfliktu:     │
  │   - Dane z bazy (v=6)        │
  │   - Swoje dane               │
  │                               │
  └── Wybiera:                    │
      - Przyjmij wersję z bazy   │
      - Ponów zapis swoich danych│
```

---

## Testowanie

### Scenariusz testowy:

1. **Użytkownik A:** Otwiera notatkę do edycji
2. **Użytkownik B:** Otwiera TĘ SAMĄ notatkę do edycji
3. **Użytkownik B:** Zapisuje zmiany (sukces)
4. **Użytkownik A:** Próbuje zapisać swoje zmiany
   - **Oczekiwany rezultat:** Panel konfliktu wersji
   - Wyświetlone dane z bazy (zapisane przez B)
   - Wyświetlone dane wprowadzone przez A
   - Możliwość wyboru: przyjąć wersję z bazy lub ponowić zapis

---

## Podsumowanie

✅ **Zrealizowane:**
- Pole `@Version` w encji `Note`
- Wyświetlanie wersji na liście notatek
- Przechwycenie `OptimisticLockException` w warstwie widoku
- Panel konfliktu z dwoma kolumnami danych
- Dwie akcje: przyjęcie wersji z bazy lub ponowienie zapisu
- Pełne tłumaczenie PL/EN

