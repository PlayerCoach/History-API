# Zadanie 2: Data stworzenia i modyfikacji dla klasy encyjnej elementu

## Wymagania
- Skonfigurować mechanizm automatycznej aktualizacji daty stworzenia
- Skonfigurować mechanizm automatycznej aktualizacji daty ostatniej modyfikacji
- Data stworzenia i data ostatniej aktualizacji wyświetlane na liście elementów

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

    // Data utworzenia - ustawiana tylko raz, nie zmienia się przy aktualizacji
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Data ostatniej modyfikacji - aktualizowana przy każdym update
    private LocalDateTime updatedAt;

    // ... pozostałe pola

    /**
     * Callback wywoływany PRZED PIERWSZYM zapisem encji do bazy.
     * Ustawia datę utworzenia i datę modyfikacji.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Callback wywoływany PRZED KAŻDĄ aktualizacją encji w bazie.
     * Aktualizuje tylko datę modyfikacji.
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

### 2. Opis adnotacji JPA Lifecycle Callbacks

| Adnotacja | Kiedy wywoływana | Zastosowanie |
|-----------|------------------|--------------|
| `@PrePersist` | Przed pierwszym `INSERT` do bazy | Ustawianie daty utworzenia, generowanie UUID |
| `@PostPersist` | Po `INSERT` do bazy | Logowanie, powiadomienia |
| `@PreUpdate` | Przed każdym `UPDATE` w bazie | Aktualizacja daty modyfikacji |
| `@PostUpdate` | Po `UPDATE` w bazie | Logowanie zmian |
| `@PreRemove` | Przed `DELETE` z bazy | Walidacja przed usunięciem |
| `@PostRemove` | Po `DELETE` z bazy | Czyszczenie powiązanych zasobów |
| `@PostLoad` | Po załadowaniu encji z bazy | Inicjalizacja pól transient |

---

### 3. Dlaczego `@Column(updatable = false)` dla `createdAt`?

```java
@Column(updatable = false)
private LocalDateTime createdAt;
```

Ta adnotacja gwarantuje, że:
- Kolumna NIE będzie uwzględniana w zapytaniach `UPDATE`
- Nawet jeśli ktoś zmieni wartość pola w kodzie, JPA NIE zaktualizuje go w bazie
- Data utworzenia pozostaje niezmienna przez cały cykl życia encji

---

### 4. Wyświetlanie dat na liście notatek

**Plik:** `notes.xhtml`

```xhtml
<h:dataTable value="#{noteListView.notes}" var="note" styleClass="table table-striped">
    
    <!-- Kolumna z tytułem -->
    <h:column>
        <f:facet name="header">#{msg['notes.column.title']}</f:facet>
        <h:link outcome="/note/note" value="#{note.title}">
            <f:param name="noteId" value="#{note.id}" />
        </h:link>
    </h:column>

    <!-- Kolumna z datą utworzenia -->
    <h:column>
        <f:facet name="header">#{msg['notes.column.createdAt']}</f:facet>
        <h:outputText value="#{note.createdAt}">
            <f:convertDateTime pattern="yyyy-MM-dd HH:mm" type="localDateTime" />
        </h:outputText>
    </h:column>

    <!-- Kolumna z datą modyfikacji -->
    <h:column>
        <f:facet name="header">#{msg['notes.column.updatedAt']}</f:facet>
        <h:outputText value="#{note.updatedAt}">
            <f:convertDateTime pattern="yyyy-MM-dd HH:mm" type="localDateTime" />
        </h:outputText>
    </h:column>

    <!-- ... pozostałe kolumny -->
</h:dataTable>
```

---

### 5. Konwerter dat dla JSF

**Ważne:** Użycie `type="localDateTime"` jest konieczne dla `LocalDateTime` z Java 8+

```xhtml
<f:convertDateTime pattern="yyyy-MM-dd HH:mm" type="localDateTime" />
```

Dostępne typy:
- `date` - dla `java.util.Date`
- `time` - dla czasu
- `both` - dla daty i czasu
- `localDate` - dla `java.time.LocalDate`
- `localTime` - dla `java.time.LocalTime`
- `localDateTime` - dla `java.time.LocalDateTime`
- `zonedDateTime` - dla `java.time.ZonedDateTime`

---

### 6. Bundle Messages

**messages_pl.properties:**
```properties
notes.column.createdAt=Data utworzenia
notes.column.updatedAt=Ostatnia modyfikacja
```

**messages_en.properties:**
```properties
notes.column.createdAt=Created at
notes.column.updatedAt=Last modified
```

---

## Przepływ czasowy

```
┌─────────────────────────────────────────────────────────────────┐
│                    CYKL ŻYCIA ENCJI                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. Tworzenie nowej notatki:                                   │
│     ┌──────────────┐                                           │
│     │ new Note()   │ → createdAt = null                        │
│     └──────────────┘   updatedAt = null                        │
│              │                                                  │
│              ▼                                                  │
│     ┌──────────────┐                                           │
│     │ @PrePersist  │ → createdAt = 2025-01-15 10:30            │
│     └──────────────┘   updatedAt = 2025-01-15 10:30            │
│              │                                                  │
│              ▼                                                  │
│     ┌──────────────┐                                           │
│     │ INSERT       │ → Zapis do bazy danych                    │
│     └──────────────┘                                           │
│                                                                 │
│  2. Aktualizacja notatki (tydzień później):                    │
│     ┌──────────────┐                                           │
│     │ @PreUpdate   │ → createdAt = 2025-01-15 10:30 (BEZ ZMIAN)│
│     └──────────────┘   updatedAt = 2025-01-22 14:45 (NOWA)     │
│              │                                                  │
│              ▼                                                  │
│     ┌──────────────┐                                           │
│     │ UPDATE       │ → createdAt NIE jest w UPDATE             │
│     └──────────────┘   (bo @Column(updatable = false))         │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Alternatywa: Hibernate @CreationTimestamp i @UpdateTimestamp

Jeśli używasz Hibernate jako implementacji JPA, możesz skorzystać z adnotacji specyficznych dla Hibernate:

```java
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
public class Note {
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

**Różnice:**
- Adnotacje Hibernate automatycznie zarządzają datami
- Nie trzeba pisać metod `@PrePersist` i `@PreUpdate`
- **Wada:** Kod jest zależny od Hibernate, nie jest przenośny między implementacjami JPA

---

## Podsumowanie

✅ **Zrealizowane:**
- Pole `createdAt` z `@Column(updatable = false)`
- Pole `updatedAt` aktualizowane przy każdej modyfikacji
- Metoda `@PrePersist` ustawiająca obie daty przy tworzeniu
- Metoda `@PreUpdate` aktualizująca tylko `updatedAt`
- Wyświetlanie dat na liście notatek z odpowiednim formatowaniem
- Pełne tłumaczenie PL/EN

