# Jak Sprawdzić Czy AJAX i Logi Działają

## 🧪 Test AJAX - Usuwanie bez przeładowania strony

### Test 1: AJAX dla usuwania postaci (kategorii)

1. **Zaloguj się jako admin**
   ```
   Login: admin
   Hasło: admin123
   ```

2. **Przejdź na listę postaci:**
   ```
   http://localhost:9080/historyapi/historicalfigure/figures.xhtml
   ```

3. **Otwórz DevTools (F12):**
   - Zakładka Network
   - Zaznacz "Preserve log"
   - Opcjonalnie filtruj: XHR

4. **Usuń postać:**
   - Kliknij przycisk "Usuń" przy dowolnej postaci
   - Potwierdź usunięcie

5. **✅ AJAX działa jeśli:**
   - Strona NIE przeładowała się całkowicie
   - W Network widzisz żądanie typu `faces-request: partial/ajax`
   - Lista postaci zaktualizowała się bez refresh
   - URL w pasku adresu się NIE zmienił
   - Status bar (dolny pasek) NIE migał

### Test 2: AJAX dla usuwania notatek (elementów)

1. **Zalogowany jako admin lub user**

2. **Wejdź w szczegóły postaci:**
   ```
   Kliknij "Podgląd" przy dowolnej postaci
   ```

3. **Otwórz DevTools (F12):**
   - Network → XHR

4. **Usuń notatkę:**
   - Kliknij "Usuń" przy notatce
   - Potwierdź

5. **✅ AJAX działa jeśli:**
   - Strona NIE przeładowała się
   - Tylko tabela z notatkami się zaktualizowała
   - W Network widzisz partial/ajax request
   - Jesteś nadal na tej samej stronie (nie przekierowanie)

### Jak rozpoznać że AJAX NIE działa:

❌ **Oznaki braku AJAX:**
- Cała strona mignie (pełne przeładowanie)
- URL zmienia się (np. dodaje `?faces-redirect=true`)
- W Network widzisz pełne żądanie HTML (nie partial)
- Pozycja scroll wraca na górę strony
- Wszystkie elementy strony są przeładowane

---

## 📋 Test Logów - Interceptor

### Gdzie znajdziesz logi:

**Lokalizacja pliku:**
```
target/liberty/wlp/usr/servers/defaultServer/logs/messages.log
```

lub szybciej w PowerShell:
```powershell
# Pokaż ostatnie 50 linii z logów
Get-Content target/liberty/wlp/usr/servers/*/logs/messages.log -Tail 50

# Filtruj tylko logi od użytkowników
Get-Content target/liberty/wlp/usr/servers/*/logs/messages.log | Select-String "User"

# Live monitoring (jak tail -f)
Get-Content target/liberty/wlp/usr/servers/*/logs/messages.log -Wait -Tail 10
```

### Test 1: Logowanie operacji CREATE (dodawanie notatki)

1. **Zaloguj się jako user lub admin**

2. **Otwórz terminal PowerShell:**
   ```powershell
   cd C:\Users\olafj\Desktop\API\History-API
   Get-Content target/liberty/wlp/usr/servers/*/logs/messages.log -Wait -Tail 20
   ```
   (Zostaw to okno otwarte - będzie pokazywać nowe logi na żywo)

3. **W przeglądarce dodaj nową notatkę:**
   - Wejdź w szczegóły postaci
   - Kliknij "Dodaj Notatkę"
   - Wypełnij formularz
   - Kliknij "Zapisz"

4. **✅ Interceptor działa jeśli w logach widzisz:**
   ```
   [INFO] User 'admin' is performing operation: CREATE/UPDATE on resource ID: abc-123-def-456
   [INFO] User 'admin' successfully completed operation: SAVE on resource ID: abc-123-def-456
   ```

### Test 2: Logowanie operacji DELETE (usuwanie notatki)

1. **Mając otwarte logi w PowerShell**

2. **Usuń notatkę w aplikacji**

3. **✅ Interceptor działa jeśli widzisz:**
   ```
   [INFO] User 'admin' is performing operation: DELETE on resource ID: abc-123-def-456
   [INFO] User 'admin' successfully completed operation: DELETE on resource ID: abc-123-def-456
   ```

### Test 3: Logowanie operacji UPDATE (edycja notatki)

1. **Edytuj istniejącą notatkę**

2. **✅ W logach powinno być:**
   ```
   [INFO] User 'user' is performing operation: CREATE/UPDATE on resource ID: abc-123-def-456
   [INFO] User 'user' successfully completed operation: SAVE on resource ID: abc-123-def-456
   ```

### Struktura logu:

Każdy log powinien zawierać:
- ✅ **Nazwę użytkownika**: np. `User 'admin'`
- ✅ **Nazwę operacji**: `CREATE/UPDATE`, `DELETE`, `SAVE`
- ✅ **ID zasobu**: UUID notatki
- ✅ **Dwa wpisy**: przed operacją ("is performing") i po ("successfully completed")

### Jeśli logi NIE działają:

❌ **Możliwe problemy:**

1. **Brak logów w ogóle:**
   - Sprawdź czy `beans.xml` ma interceptor
   - Sprawdź czy `NoteService` ma adnotacje `@Logged`

2. **Username to "UNKNOWN":**
   - SecurityContext nie działa
   - Użytkownik nie jest zalogowany
   - Problem z sesją

3. **Resource ID to null:**
   - Problem z ekstrakcją ID z parametrów
   - Sprawdź czy metoda dostaje właściwe parametry

---

## 📊 Przykładowy pełny test flow:

### Scenariusz: Zalogowany admin usuwa notatkę

**Krok 1: Przygotowanie**
```powershell
# Terminal 1: Uruchom serwer
mvnw liberty:run

# Terminal 2: Monitoruj logi
Get-Content target/liberty/wlp/usr/servers/*/logs/messages.log -Wait -Tail 20
```

**Krok 2: W przeglądarce (z DevTools F12)**
1. Zaloguj jako admin
2. Wejdź na listę postaci
3. Wejdź w szczegóły postaci (np. Napoleon)
4. Otwórz DevTools → Network → XHR
5. Kliknij "Usuń" przy notatce
6. Potwierdź

**Krok 3: Weryfikacja**

✅ **AJAX działa jeśli:**
- [ ] W DevTools widzisz żądanie typu `partial/ajax`
- [ ] Strona nie przeładowała się
- [ ] Lista notatek zaktualizowała się
- [ ] Notatka zniknęła z listy

✅ **Logi działają jeśli w terminalu 2 widzisz:**
```
[INFO] User 'admin' is performing operation: DELETE on resource ID: [UUID]
[INFO] User 'admin' successfully completed operation: DELETE on resource ID: [UUID]
```

---

## 🎯 Szybki test wszystkiego naraz:

```powershell
# 1. Zbuduj i uruchom
mvnw clean package
mvnw liberty:run

# 2. W drugim terminalu:
Get-Content target/liberty/wlp/usr/servers/*/logs/messages.log | Select-String "User"

# 3. W przeglądarce:
# - Zaloguj się
# - Usuń notatkę (sprawdź AJAX - strona nie mignie)
# - Sprawdź terminal - powinny być 2 linie z "User 'xxx' ... DELETE"

# 4. Jeśli widzisz logi z nazwą użytkownika i UUID:
# ✅ Wszystko działa!
```

---

## 🐛 Troubleshooting:

### AJAX nie działa:
- Sprawdź `<f:ajax execute="@this" render="notesTable noteMessages"/>` w widoku
- Sprawdź czy metoda zwraca `null` zamiast String z nawigacją
- Sprawdź console w przeglądarce czy są błędy JS

### Logi nie pokazują się:
- Sprawdź czy serwer jest uruchomiony
- Sprawdź ścieżkę do pliku logów
- Sprawdź czy wykonujesz operacje na notatkach (nie na postaciach - te nie mają @Logged)

### Username to "UNKNOWN":
- Użytkownik nie jest zalogowany
- Problem z SecurityContext injection

---

## ✨ Wszystko działa jeśli:

- ✅ Usuwanie notatki BEZ przeładowania strony
- ✅ W logach widzisz nazwę użytkownika i UUID
- ✅ Dwa wpisy w logu: przed i po operacji
- ✅ DevTools pokazuje partial/ajax request

