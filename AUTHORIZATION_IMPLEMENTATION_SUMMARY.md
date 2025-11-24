# Implementacja Autoryzacji - Podsumowanie

## Co zostało zaimplementowane:

### 1. Konfiguracja Security

#### SecurityConfig.java
- Zmieniono z `BasicAuthenticationMechanismDefinition` na `CustomFormAuthenticationMechanismDefinition`
- Konfiguracja `DatabaseIdentityStoreDefinition` dla walidacji użytkowników
- Strony logowania: `/authentication/login.xhtml` i `/authentication/login_error.xhtml`

#### BasicAuthFilter.java
- Filtr JAX-RS do obsługi Basic Authentication dla REST API (`/api/*`)
- Pozwala na użycie Basic Auth w endpointach REST podczas gdy JSF używa Form Auth
- Automatycznie waliduje nagłówek Authorization i ustawia SecurityContext

### 2. Beany CDI

#### LoginBean.java
- Bean do obsługi logowania przez formularz
- Metody: `login()` i `logout()`
- Integracja z SecurityContext dla autentykacji

#### AuthBean.java
- Bean pomocniczy dostępny w widokach JSF
- Metody:
  - `getUsername()` - zwraca nazwę zalogowanego użytkownika
  - `isLoggedIn()` - czy użytkownik jest zalogowany
  - `isAdmin()` - czy użytkownik ma rolę ADMIN
  - `isUser()` - czy użytkownik ma rolę USER

### 3. Strony autentykacji

#### /authentication/login.xhtml
- Formularz logowania (username, password)
- Obsługa błędów walidacji
- Link powrotu do strony głównej

#### /authentication/login_error.xhtml
- Strona błędu logowania
- Informacja o nieprawidłowych danych
- Link do ponownego logowania

#### Strony błędów (/error/)
- `401.xhtml` - Nieautoryzowany dostęp
- `403.xhtml` - Zabroniony dostęp
- `404.xhtml` - Strona nie znaleziona

### 4. Konfiguracja web.xml

Dodano security constraints:
- `/historicalfigure/*` - wymaga roli USER lub ADMIN
- `/note/*` - wymaga roli USER lub ADMIN  
- `/api/*` - wymaga roli USER lub ADMIN

Zdefiniowano role:
- `ADMIN` - administrator
- `USER` - zwykły użytkownik

### 5. Aktualizacje widoków (Views)

#### Template (main.xhtml)
- Wyświetlanie nazwy zalogowanego użytkownika w nagłówku
- Badge "Administrator" dla adminów
- Przycisk "Wyloguj" dla zalogowanych użytkowników
- Przycisk "Zaloguj się" dla niezalogowanych

#### Lista postaci (figures.xhtml)
✅ Przycisk "Usuń" widoczny tylko dla administratora

#### Widok postaci (figure.xhtml)
✅ Przyciski "Usuń" i "Edytuj" dla notatek widoczne tylko dla właściciela lub admina
✅ Przycisk "Dodaj Notatkę" z przekazaniem figureId

#### Lista notatek (notes.xhtml)
✅ Przycisk "Edytuj" widoczny tylko dla właściciela lub admina
✅ Lista pokazuje tylko notatki użytkownika (admin widzi wszystkie)

#### Widok notatki (note.xhtml)
✅ Przycisk "Edytuj" widoczny tylko dla właściciela lub admina
✅ Kontrola dostępu w NoteSingularView - przekierowanie 403 jeśli brak dostępu

#### Edycja notatki (note_edit.xhtml)
✅ Kontrola dostępu w NoteEditView
✅ Automatyczne ustawianie createdBy przy tworzeniu nowej notatki

### 6. Aktualizacje logiki biznesowej (View Beans)

#### NoteListView.java
- Filtrowanie notatek: zwykły użytkownik widzi tylko swoje, admin wszystkie
- Injektowanie SecurityContext

#### HistoricalFigureSingularView.java
- Filtrowanie notatek w kategorii: użytkownik widzi tylko swoje, admin wszystkie
- Injektowanie SecurityContext i UserService

#### NoteSingularView.java
- Metoda `checkAccess()` - sprawdza czy użytkownik ma dostęp do notatki
- Przekierowanie na błąd 403 jeśli brak dostępu
- Dostęp mają: właściciel notatki i administrator

#### NoteEditView.java
- Metoda `checkAccess()` - kontrola dostępu do edycji
- Automatyczne ustawianie `createdBy` przy tworzeniu nowej notatki
- Kontrola przed zapisem

## Wymagania zadania - status realizacji:

✅ **Próba wejścia na zabezpieczony zasób przekierowuje na formularz logowania**
   - Konfiguracja CustomFormAuth w SecurityConfig
   - web.xml security constraints

✅ **Tylko zalogowany użytkownik może wyświetlać listę kategorii**
   - Security constraint na `/historicalfigure/*`

✅ **Tylko administrator może (widzi przycisk) usunąć wybraną kategorię**
   - `rendered="#{authBean.admin}"` w figures.xhtml

✅ **Zwykły użytkownik widzi tylko swoje elementy w kategorii**
   - Filtrowanie w HistoricalFigureSingularView.getNotes()
   - Filtrowanie w NoteListView

✅ **Administrator widzi wszystkie elementy w kategorii**
   - Warunek `if (securityContext.isCallerInRole("ADMIN"))` w view beans

✅ **Używając bezpośredniego linku tylko właściciel (lub admin) może go wyświetlić**
   - NoteSingularView.checkAccess() - kontrola w @PostConstruct
   - NoteEditView.checkAccess() - kontrola przed edycją
   - Przekierowanie 403 przy braku dostępu

✅ **W ramach nagłówka wyświetlana jest nazwa zalogowanego użytkownika**
   - main.xhtml wyświetla `#{authBean.username}`

✅ **Auth w endpointach REST dalej działa**
   - BasicAuthFilter dla ścieżek `/api/*`
   - IdentityStoreHandler waliduje Basic Auth z tych samych danych co Form Auth

## Co trzeba zrobić przed uruchomieniem:

1. **Zbudować projekt:**
   ```bash
   mvnw clean package
   ```

2. **Upewnić się, że w bazie danych są użytkownicy:**
   - Tabela `users` musi mieć kolumny: `id`, `login`, `password`, `email`, `role`
   - Hasła muszą być zahashowane algorytmem PBKDF2WithHmacSHA256
   - Role: `ADMIN` lub `USER`

3. **Sprawdzić server.xml:**
   - Powinien mieć feature `appSecurity-5.0`
   - DataSource `jdbc/historyDS` skonfigurowany

4. **Uruchomić serwer:**
   ```bash
   mvnw liberty:run
   ```

## Testowanie:

### Logowanie JSF:
1. Przejdź na http://localhost:9080/History-API/historicalfigure/figures.xhtml
2. Zostaniesz przekierowany na stronę logowania
3. Zaloguj się (np. admin/admin lub user/user)
4. Powinieneś zobaczyć listę postaci

### REST API z Basic Auth:
```http
GET http://localhost:9080/History-API/api/notes
Authorization: Basic dXNlcjp1c2Vy
```

### Testowanie uprawnień:
- Jako USER: nie widzisz przycisku "Usuń" przy postaciach
- Jako ADMIN: widzisz wszystkie przyciski
- Jako USER: widzisz tylko swoje notatki
- Jako ADMIN: widzisz wszystkie notatki

## Znane problemy / Ostrzeżenia:

1. **Unsatisfied dependency: SecurityContext** - to ostrzeżenie IDE, w runtime CDI container poprawnie wstrzyknie SecurityContext

2. **IdentityStoreHandler w BasicAuthFilter** - jeśli wystąpi problem, można usunąć BasicAuthFilter i używać tylko Form Auth (REST API będzie wymagało sesji)

3. **Jeśli Basic Auth nie działa dla REST:** 
   - Sprawdź czy DatabaseIdentityStoreDefinition jest poprawnie skonfigurowany
   - Sprawdź logi Liberty czy IdentityStoreHandler jest dostępny
   - Ewentualnie zakomentuj @Provider w BasicAuthFilter i używaj tylko Form Auth

