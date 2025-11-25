# Quick Start - Uruchomienie po implementacji 3 zadań

## ✅ Co jest gotowe:

1. ✅ **Lokalizacja językowa (i18n)** - 2 języki (pl/en), dynamiczne tło
2. ✅ **AJAX dla usuwania** - bez przeładowania strony
3. ✅ **Logging interceptor** - logowanie CRUD z nazwą użytkownika i ID zasobu

## 🚀 Kroki uruchomienia:

### 1. Zbuduj projekt:
```powershell
mvnw clean package
```

### 2. Uruchom serwer:
```powershell
mvnw liberty:run
```

### 3. Otwórz w przeglądarce:
```
http://localhost:9080/historyapi/
```

### 4. Zaloguj się:
**Administrator:**
- Login: `admin`
- Hasło: `admin123`

**Zwykły użytkownik:**
- Login: `test`
- Hasło: `password123`

---

## 🧪 Szybkie testy:

### Test lokalizacji:
1. Otwórz aplikację → powinny być polskie teksty i tło `background_pl.png`
2. Zmień język przeglądarki na angielski i odśwież → angielskie teksty i tło `background.png`

### Test AJAX:
1. Zaloguj jako admin
2. Usuń postać z listy → lista się zaktualizuje BEZ przeładowania strony
3. Wejdź w postać i usuń notatkę → lista notatek się zaktualizuje BEZ przeładowania

### Test logowania:
1. Zaloguj się
2. Dodaj/usuń notatkę
3. Sprawdź logi:
```powershell
Get-Content target/liberty/wlp/usr/servers/*/logs/messages.log | Select-String "User"
```

Powinny być wpisy typu:
```
User 'admin' is performing operation: DELETE on resource ID: xxx
User 'admin' successfully completed operation: DELETE on resource ID: xxx
```

---

## 📋 Struktura tłumaczeń:

Wszystkie teksty są w:
- `src/main/resources/bundles/messages_pl.properties` (polski)
- `src/main/resources/bundles/messages_en.properties` (angielski)

Użycie w widokach:
```xhtml
#{msg['app.header.title']}
#{msg['figures.list.title']}
#{msg['notes.action.delete']}
```

---

## 🎯 Weryfikacja punktów:

| Funkcjonalność | Gdzie sprawdzić | Punkty |
|----------------|-----------------|--------|
| Tłumaczenia tekstów | Wszystkie strony | 0.5 |
| Dynamiczne tło wg języka | Nagłówek strony | 0.5 |
| AJAX usuwanie kategorii | Lista postaci | 0.5 |
| AJAX usuwanie elementów | Notatki w postaci | 0.5 |
| Logging przed operacją | Logi serwera | 0.5 |
| Logging po operacji | Logi serwera | 0.5 |

---

## ⚡ Możliwe problemy:

### Problem: Brak tłumaczeń
**Rozwiązanie:** Sprawdź czy faces-config.xml jest poprawnie skonfigurowany

### Problem: AJAX nie działa
**Rozwiązanie:** Sprawdź konsolę przeglądarki, upewnij się że formularze mają id

### Problem: Logi nie pojawiają się
**Rozwiązanie:** 
- Sprawdź czy beans.xml ma interceptor
- Sprawdź czy metody mają @Logged
- Spójrz do messages.log, nie console.log

---

## 📝 Wszystko gotowe!

Aplikacja jest w pełni funkcjonalna z:
- ✅ Autoryzacją (Custom Form + Basic Auth dla REST)
- ✅ Lokalizacją (pl/en z dynamicznym tłem)
- ✅ AJAX (usuwanie bez reload)
- ✅ Loggingiem (interceptor z informacją o użytkowniku)

**Możesz uruchomić i testować!** 🎉

