# Checklist przed uruchomieniem - Autoryzacja

## ✅ Zaimplementowane funkcjonalności:

### Konfiguracja:
- [x] SecurityConfig z CustomFormAuthenticationMechanismDefinition
- [x] DatabaseIdentityStoreDefinition dla walidacji użytkowników
- [x] BasicAuthFilter dla REST API
- [x] web.xml z security-constraints
- [x] Strony logowania (login.xhtml, login_error.xhtml)
- [x] Strony błędów (401, 403, 404)

### Beany:
- [x] LoginBean - obsługa logowania/wylogowania
- [x] AuthBean - helper do sprawdzania ról w widokach

### Widoki JSF:
- [x] main.xhtml - wyświetlanie użytkownika w nagłówku
- [x] figures.xhtml - przycisk "Usuń" tylko dla admina
- [x] figure.xhtml - przyciski edycji/usuwania notatek z kontrolą dostępu
- [x] notes.xhtml - filtrowanie notatek, kontrola przycisków
- [x] note.xhtml - przycisk edycji z kontrolą dostępu
- [x] note_edit.xhtml - kontrola dostępu do edycji

### Logika biznesowa:
- [x] NoteListView - filtrowanie notatek (user widzi swoje, admin wszystkie)
- [x] HistoricalFigureSingularView - filtrowanie notatek w kategorii
- [x] NoteSingularView - kontrola dostępu w @PostConstruct
- [x] NoteEditView - kontrola dostępu, automatyczne ustawianie createdBy

## 📋 Co zrobić przed uruchomieniem:

### 1. Zbuduj projekt:
```bash
mvnw clean package
```

### 2. Utwórz użytkowników testowych:

**Opcja A: Utwórz klasę InitialDataLoader** (zalecane)
   
Skopiuj kod z pliku `TEST_USERS_SETUP.md` i utwórz klasę:
`src/main/java/pl/edu/pg/eti/kask/historyapi/configuration/InitialDataLoader.java`

**Opcja B: Użyj REST API** 

Jeśli masz już endpoint do tworzenia użytkowników, użyj go.

### 3. Sprawdź server.xml:
```xml
<featureManager>
    <feature>appSecurity-5.0</feature>
    <!-- pozostałe features -->
</featureManager>

<dataSource id="H2DataSource" jndiName="jdbc/historyDS">
    <!-- konfiguracja -->
</dataSource>
```

### 4. Uruchom serwer:
```bash
mvnw liberty:run
```

## 🧪 Plan testowania:

### Test 1: Przekierowanie na login
1. Otwórz: http://localhost:9080/History-API/historicalfigure/figures.xhtml
2. ✅ Powinno przekierować na `/authentication/login.xhtml`

### Test 2: Logowanie jako admin
1. Zaloguj się: admin/admin
2. ✅ Przejdź na listę postaci
3. ✅ Widzisz przycisk "Usuń" przy postaciach
4. ✅ Widzisz wszystkie notatki
5. ✅ Widzisz przyciski edycji/usuwania przy wszystkich notatkach

### Test 3: Logowanie jako user
1. Wyloguj się
2. Zaloguj się: user/user
3. ✅ Przejdź na listę postaci
4. ✅ NIE widzisz przycisku "Usuń" przy postaciach
5. ✅ Widzisz tylko swoje notatki
6. ✅ Widzisz przyciski edycji/usuwania tylko przy swoich notatkach

### Test 4: Próba dostępu do cudzej notatki
1. Zalogowany jako user
2. Spróbuj wejść bezpośrednim linkiem na notatkę admina
3. ✅ Powinno wyświetlić błąd 403

### Test 5: REST API z Basic Auth
```http
GET http://localhost:9080/History-API/api/notes
Authorization: Basic YWRtaW46YWRtaW4=
```
✅ Powinno zwrócić listę notatek

### Test 6: Nagłówek - wyświetlanie użytkownika
1. Zalogowany użytkownik
2. ✅ W prawym górnym rogu widoczne: "Zalogowany: [username]"
3. ✅ Dla admina badge "Administrator"
4. ✅ Przycisk "Wyloguj"

### Test 7: Wylogowanie
1. Kliknij "Wyloguj"
2. ✅ Przekierowanie na stronę główną
3. ✅ Próba wejścia na zabezpieczoną stronę przekierowuje na login

## ⚠️ Znane ostrzeżenia (można zignorować):

- "Unsatisfied dependency: SecurityContext" - to ostrzeżenie IDE, działa w runtime
- "Cannot resolve directory 'error'" - IDE nie widzi katalogu, ale pliki istnieją
- "Class 'SecurityConfig' is never used" - używana przez CDI container

## 🔧 Debugging:

### Problem: Basic Auth nie działa w REST API

**Rozwiązanie 1:** Sprawdź logi Liberty:
```
grep "CWWKS" target/liberty/wlp/usr/servers/*/logs/messages.log
```

**Rozwiązanie 2:** Tymczasowo wyłącz BasicAuthFilter:
- Zakomentuj `@Provider` w BasicAuthFilter.java
- REST API będzie wymagało sesji z JSF

### Problem: Strona logowania w pętli

**Możliwe przyczyny:**
1. Niepoprawne hashe haseł w bazie
2. Nieprawidłowe query w DatabaseIdentityStoreDefinition
3. Brak użytkowników w bazie

**Sprawdź:**
```sql
SELECT login, password, role FROM users;
```

### Problem: 403 dla wszystkich zasobów

**Sprawdź:**
1. Czy role w bazie to dokładnie "ADMIN" i "USER" (wielkość liter)
2. Czy groupsQuery w SecurityConfig zwraca poprawne role
3. Czy security-role w web.xml zgadzają się z rolami w bazie

## 📝 Notatki:

- Hasła w bazie MUSZĄ być zahashowane przez PBKDF2WithHmacSHA256
- UserService.save() automatycznie hashuje hasła
- Role: dokładnie "ADMIN" lub "USER" (wielkość liter ma znaczenie)
- Basic Auth działa tylko dla ścieżek `/api/*`
- Form Auth dla wszystkich stron JSF

## ✨ Gotowe do testowania!

Po wykonaniu kroków 1-4, aplikacja powinna być gotowa do testowania wszystkich funkcjonalności autoryzacji.

