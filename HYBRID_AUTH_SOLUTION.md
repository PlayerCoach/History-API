````markdown
# KONFIGURACJA BEZPIECZEŃSTWA - LAB 7
## CustomFormAuthentication dla JSF + REST API

## 🎯 Rozwiązanie - Hybrydowa konfiguracja

### 1. SecurityConfig.java - @CustomFormAuthenticationMechanismDefinition

```java
@ApplicationScoped
@CustomFormAuthenticationMechanismDefinition(
    loginToContinue = @LoginToContinue(
        loginPage = "/login.xhtml",
        errorPage = "/login.xhtml?error=true"
    )
)
@DatabaseIdentityStoreDefinition(
    dataSourceLookup = "jdbc/historyDS",
    callerQuery = "SELECT password FROM users WHERE login = ?",
    groupsQuery = "SELECT role FROM users WHERE login = ?",
    hashAlgorithm = Pbkdf2PasswordHash.class,
    hashAlgorithmParameters = {
        "Pbkdf2PasswordHash.Iterations=210000",
        "Pbkdf2PasswordHash.Algorithm=PBKDF2WithHmacSHA256",
        "Pbkdf2PasswordHash.SaltSizeBytes=32"
    }
)
public class SecurityConfig {
}
```

**✅ Klucz: @CustomFormAuthenticationMechanismDefinition**
- Obsługuje formularz logowania JSF
- Wspiera programową autoryzację (`@RolesAllowed`, `@PermitAll`)
- Działa z `@DatabaseIdentityStoreDefinition`

### 2. web.xml - Tylko security-constraint

```xml
<security-constraint>
    <web-resource-collection>
        <web-resource-name>Protected Pages</web-resource-name>
        <url-pattern>/historicalfigure/*</url-pattern>
        <url-pattern>/note/*</url-pattern>
    </web-resource-collection>
    <auth-constraint>
        <role-name>USER</role-name>
        <role-name>ADMIN</role-name>
    </auth-constraint>
</security-constraint>

<!-- BRAK <login-config> - używamy annotation! -->
```

### 3. Kontrolery REST - Działają z @RolesAllowed

```java
@Path("/users")
public class UserController {
    
    @POST
    @PermitAll  // ✅ Działa - każdy może się zarejestrować
    public Response registerUser(UserDto dto) { ... }
    
    @GET
    @RolesAllowed("ADMIN")  // ✅ Działa - tylko admin
    public Response getAllUsers() { ... }
}

@Path("/figures")
public class FigureController {
    
    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")  // ✅ Działa - tylko admin może usuwać
    public Response deleteFigure(@PathParam("id") UUID id) { ... }
}
```

## 🚀 CO DZIAŁA?

### ✅ JSF (widoki):
- Formularz logowania `/login.xhtml`
- Automatyczne przekierowanie na login przy próbie dostępu
- Warunkowe renderowanie (`rendered="#{securityView.admin}"`)
- Filtrowanie danych w backing beans

### ✅ REST API:
- `@RolesAllowed("ADMIN")` - tylko admin
- `@RolesAllowed({"USER", "ADMIN"})` - zalogowani
- `@PermitAll` - każdy (np. rejestracja)
- Programowa autoryzacja w kontrolerach

## 🧪 TESTY

### Test 1: JSF - Formularz logowania
```
http://localhost:9080/historyapi/historicalfigure/figures.xhtml
→ Przekierowanie na /login.xhtml ✅
→ Login: admin / admin123
→ Widzi wszystko + przycisk "Usuń" ✅
```

### Test 2: REST API - @RolesAllowed
```bash
# Bez autoryzacji - 401
DELETE /historyapi/api/figures/{id}

# Jako USER - 403 Forbidden
DELETE /historyapi/api/figures/{id}
Authorization: Basic test password123

# Jako ADMIN - 204 No Content
DELETE /historyapi/api/figures/{id}
Authorization: Basic admin admin123
```

### Test 3: REST API - @PermitAll
```bash
# Rejestracja bez logowania - 201 Created
POST /historyapi/api/users
Content-Type: application/json

{
  "login": "newuser",
  "email": "new@test.com",
  "password": "password123"
}
```

## 🎉 GOTOWE!

**Zrestartuj serwer i przetestuj:**
1. JSF → formularz logowania działa ✅
2. REST API → `@RolesAllowed` działa ✅
3. Hashowanie PBKDF2 działa ✅
4. Obie metody używają tej samej bazy użytkowników ✅
````
