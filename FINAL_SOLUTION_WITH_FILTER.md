# ✅ FINALNE ROZWIĄZANIE - FORM + BASIC AUTH

## 🎯 Konfiguracja hybrydowa (JSF FORM + REST Basic Auth)

### Implementacja

#### 1. SecurityConfig.java
```java
@ApplicationScoped
@DatabaseIdentityStoreDefinition(
    dataSourceLookup = "jdbc/historyDS",
    callerQuery = "SELECT password FROM users WHERE login = ?",
    groupsQuery = "SELECT role FROM users WHERE login = ?",
    hashAlgorithm = Pbkdf2PasswordHash.class,
    hashAlgorithmParameters = {...}
)
public class SecurityConfig { }
```
- Tylko `@DatabaseIdentityStoreDefinition`
- BRAK annotation dla mechanizmu uwierzytelniania

#### 2. web.xml
```xml
<security-constraint>
    <url-pattern>/historicalfigure/*</url-pattern>
    <url-pattern>/note/*</url-pattern>
    ...
</security-constraint>

<login-config>
    <auth-method>FORM</auth-method>
    <form-login-page>/login.xhtml</form-login-page>
    <form-error-page>/login.xhtml?error=true</form-error-page>
</login-config>
```
- FORM authentication dla JSF
- **BRAK** security-constraint dla `/api/*`

#### 3. BasicAuthFilter.java (NOWY!)
```java
@WebFilter(urlPatterns = "/api/*")
public class BasicAuthFilter implements Filter {
    @Override
    public void doFilter(...) {
        // Sprawdza header "Authorization: Basic ..."
        // Wywołuje httpRequest.login(username, password)
        // Pozwala REST API używać Basic Auth!
    }
}
```

## 🚀 Jak to działa?

### JSF (widoki):
1. Użytkownik: `/historicalfigure/figures.xhtml`
2. Przekierowanie → `/login.xhtml`
3. Formularz → `j_security_check`
4. Sesja HTTP utworzona ✅

### REST API:
1. Request: `GET /api/users` + `Authorization: Basic admin:admin123`
2. BasicAuthFilter przechwytuje ✅
3. Dekoduje Base64 → `admin:admin123`
4. Wywołuje `httpRequest.login("admin", "admin123")` ✅
5. `@RolesAllowed("ADMIN")` w kontrolerze sprawdza role ✅

## 🧪 TESTY

### Test 1: JSF
```
http://localhost:9080/historyapi/historicalfigure/figures.xhtml
→ Formularz logowania ✅
→ admin/admin123
→ Lista kategorii ✅
```

### Test 2: REST API
```bash
curl -X GET http://localhost:9080/historyapi/api/users \
  -H 'Authorization: Basic YWRtaW46YWRtaW4xMjM=' \
  -H 'Accept: application/json'

# Powinno zwrócić JSON z listą użytkowników ✅
```

## 🎉 GOTOWE!

**Zrestartuj serwer i przetestuj oba scenariusze!**

Konta:
- admin / admin123
- test / password123

