# Implementacja Zadań Lab - Podsumowanie

## ✅ Zrealizowane zadania:

### 1. Lokalizacja językowa aplikacji (i18n) - 1 pkt

**Co zostało zrobione:**

#### Pliki tłumaczeń:
- `src/main/resources/bundles/messages_pl.properties` - polskie tłumaczenia
- `src/main/resources/bundles/messages_en.properties` - angielskie tłumaczenia

#### Konfiguracja:
- `src/main/webapp/WEB-INF/faces-config.xml` - konfiguracja locale
  - Domyślny język: polski (pl)
  - Obsługiwane: pl, en
  - Resource bundle: `bundles.messages` jako `msg`

#### Bean LocaleBean:
- `src/main/java/pl/edu/pg/eti/kask/historyapi/configuration/LocaleBean.java`
- Automatyczne wykrywanie języka przeglądarki
- Metoda `getBackgroundImage()` zwraca:
  - `background_pl.png` dla języka polskiego
  - `background.png` dla języka angielskiego

#### Zmodyfikowane widoki z tłumaczeniami:
- ✅ `main.xhtml` - szablon (nagłówek, nawigacja, stopka)
- ✅ `figures.xhtml` - lista postaci
- ✅ `figure.xhtml` - pojedyncza postać
- ✅ `notes.xhtml` - lista notatek
- ✅ `note.xhtml` - pojedyncza notatka
- ✅ `note_edit.xhtml` - edycja notatki
- ✅ `login.xhtml` - strona logowania
- ✅ `login_error.xhtml` - błąd logowania
- ✅ `401.xhtml`, `403.xhtml`, `404.xhtml` - strony błędów

#### Dynamiczne tło:
W `main.xhtml` użyto:
```xhtml
background-image: url(#{resource['default:images/'.concat(localeBean.backgroundImage)]});
```

**Testowanie:**
1. Zmień język przeglądarki na polski → zobaczysz polskie teksty i `background_pl.png`
2. Zmień język na angielski → zobaczysz angielskie teksty i `background.png`

---

### 2. Usuwanie z wykorzystaniem AJAX - 1 pkt

**Co zostało zrobione:**

#### Usuwanie kategorii (postaci historycznych):
W `figures.xhtml`:
```xhtml
<h:commandButton value="#{msg['figures.action.delete']}" 
                 action="#{figureListView.deleteFigure(fig.id)}"
                 onclick="return confirm(...);">
    <f:ajax execute="@this" render="figuresTable messages"/>
</h:commandButton>
```

#### Usuwanie elementów (notatek):
W `figure.xhtml`:
```xhtml
<h:commandButton value="#{msg['notes.action.delete']}"
                 action="#{figureDetailView.deleteNote(note.id)}"
                 onclick="return confirm(...);">
    <f:ajax execute="@this" render="notesTable noteMessages"/>
</h:commandButton>
```

#### Zmodyfikowane metody w bean'ach:
**FigureListView.java:**
```java
public String deleteFigure(UUID id) {
    figureService.delete(id);
    figures = null; // Reset listy dla AJAX
    return null; // Dla AJAX zwracamy null
}
```

**HistoricalFigureSingularView.java:**
```java
public String deleteNote(UUID noteId) {
    noteService.delete(noteId);
    notes = null; // Reset listy dla AJAX
    return null; // Dla AJAX zwracamy null
}
```

**Efekt:**
- Nie ma przeładowania całej strony
- Aktualizowana jest tylko tabela z listą
- Wyświetlany jest komunikat o powodzeniu/błędzie

---

### 3. Logging (Interceptor) - 1 pkt

**Co zostało zrobione:**

#### Interceptor:
- `src/main/java/pl/edu/pg/eti/kask/historyapi/interceptor/LoggingInterceptor.java`
- Loguje operacje: CREATE, UPDATE, DELETE
- Format logów:
  ```
  User 'admin' is performing operation: DELETE on resource ID: xxx
  User 'admin' successfully completed operation: DELETE on resource ID: xxx
  ```

#### Adnotacja @Logged:
- `src/main/java/pl/edu/pg/eti/kask/historyapi/interceptor/Logged.java`
- `@InterceptorBinding` do oznaczania metod do logowania

#### Aktywacja w beans.xml:
```xml
<interceptors>
    <class>pl.edu.pg.eti.kask.historyapi.interceptor.LoggingInterceptor</class>
</interceptors>
```

#### Użycie w NoteService:
```java
@Logged
public void delete(UUID id) { ... }

@Logged
public void save(Note note) { ... }

@Logged
public void createNote(Note note, HistoricalFigure figure, User owner) { ... }
```

**Informacje logowane:**
- ✅ Nazwa użytkownika (z SecurityContext)
- ✅ Nazwa operacji (CREATE/UPDATE/DELETE)
- ✅ Identyfikator zasobu (UUID)

**Przykładowy log:**
```
INFO: User 'admin' is performing operation: DELETE on resource ID: 25b8c1f0-7ac1-11eb-8000-0242ac110002
INFO: User 'admin' successfully completed operation: DELETE on resource ID: 25b8c1f0-7ac1-11eb-8000-0242ac110002
```

---

## 📁 Utworzone/Zmodyfikowane pliki:

### Nowe pliki:
1. `src/main/resources/bundles/messages_pl.properties`
2. `src/main/resources/bundles/messages_en.properties`
3. `src/main/webapp/WEB-INF/faces-config.xml`
4. `src/main/java/pl/edu/pg/eti/kask/historyapi/configuration/LocaleBean.java`
5. `src/main/java/pl/edu/pg/eti/kask/historyapi/interceptor/LoggingInterceptor.java`
6. `src/main/java/pl/edu/pg/eti/kask/historyapi/interceptor/Logged.java`

### Zmodyfikowane pliki:
1. `src/main/webapp/WEB-INF/template/main.xhtml` - tłumaczenia + dynamiczne tło
2. `src/main/webapp/historicalfigure/figures.xhtml` - tłumaczenia + AJAX
3. `src/main/webapp/historicalfigure/figure.xhtml` - tłumaczenia + AJAX
4. `src/main/webapp/note/notes.xhtml` - tłumaczenia
5. `src/main/webapp/note/note.xhtml` - tłumaczenia
6. `src/main/webapp/note/note_edit.xhtml` - tłumaczenia
7. `src/main/webapp/authentication/login.xhtml` - tłumaczenia
8. `src/main/webapp/authentication/login_error.xhtml` - tłumaczenia
9. `src/main/webapp/error/401.xhtml` - tłumaczenia
10. `src/main/webapp/error/403.xhtml` - tłumaczenia
11. `src/main/webapp/error/404.xhtml` - tłumaczenia
12. `src/main/webapp/WEB-INF/beans.xml` - aktywacja interceptora
13. `src/main/java/.../view/historicalfigure/HistoricalFigureListView.java` - AJAX
14. `src/main/java/.../view/historicalfigure/HistoricalFigureSingularView.java` - AJAX
15. `src/main/java/.../note/service/NoteService.java` - @Logged

---

## 🧪 Plan testowania:

### Test 1: Lokalizacja językowa
1. **Polski (domyślny):**
   - Otwórz aplikację
   - Sprawdź czy teksty są po polsku
   - Sprawdź czy tło to `background_pl.png`

2. **Angielski:**
   - Zmień ustawienia języka przeglądarki na angielski
   - Odśwież stronę
   - Sprawdź czy teksty są po angielsku
   - Sprawdź czy tło to `background.png`

### Test 2: AJAX przy usuwaniu
1. **Usuwanie kategorii:**
   - Zaloguj się jako admin
   - Przejdź na listę postaci
   - Usuń postać
   - ✅ Strona NIE powinna się przeładować
   - ✅ Lista powinna się zaktualizować
   - ✅ Postać powinna zniknąć z listy

2. **Usuwanie elementu:**
   - Wejdź w szczegóły postaci
   - Usuń notatkę
   - ✅ Strona NIE powinna się przeładować
   - ✅ Lista notatek powinna się zaktualizować

### Test 3: Logging
1. **Dodawanie notatki:**
   - Zaloguj się jako użytkownik
   - Dodaj nową notatkę
   - Sprawdź logi: `target/liberty/wlp/usr/servers/*/logs/messages.log`
   - Powinien być wpis: `User 'test' is performing operation: CREATE/UPDATE on resource ID: xxx`

2. **Usuwanie notatki:**
   - Usuń notatkę
   - Sprawdź logi
   - Powinny być 2 wpisy: przed i po operacji

3. **Sprawdzanie logów:**
   ```powershell
   Get-Content target/liberty/wlp/usr/servers/*/logs/messages.log | Select-String "User"
   ```

---

## ✨ Podsumowanie punktacji:

| Zadanie | Punkty | Status |
|---------|--------|--------|
| Lokalizacja językowa (teksty) | 0.5 | ✅ |
| Lokalizacja językowa (obrazki) | 0.5 | ✅ |
| AJAX usuwanie kategorii | 0.5 | ✅ |
| AJAX usuwanie elementów | 0.5 | ✅ |
| Logging operacji CRUD | 0.5 | ✅ |
| Logging z pełnymi danymi | 0.5 | ✅ |
| **RAZEM** | **3.0** | ✅ |

---

## 🔧 Uwagi techniczne:

1. **LocaleBean** automatycznie wykrywa język przeglądarki przez `getRequestLocale()`
2. **AJAX** używa `<f:ajax>` z atrybutami `execute` i `render`
3. **Interceptor** działa dla wszystkich metod oznaczonych `@Logged`
4. **SecurityContext** w interceptorze może być `null` dla niezalogowanych użytkowników (wtedy username = "UNKNOWN")

---

## 📝 Gotowe do testowania!

Wszystkie 3 zadania zostały w pełni zaimplementowane zgodnie z wymaganiami.

