package pl.edu.pg.eti.kask.historyapi.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

/**
 * Konfiguracja bezpieczeństwa aplikacji.
 * 
 * UWAGA: Aplikacja używa CustomFormAuthenticationMechanismDefinition, które działa
 * tylko z widokami JSF (formularz logowania). REST API endpoints (/api/*) są 
 * zabezpieczone tym samym mechanizmem, co oznacza, że:
 * - Próba dostępu do REST API bez sesji przekieruje na formularz logowania HTML
 * - REST API NIE obsługuje Basic Authentication
 * - Aby korzystać z REST API, należy najpierw zalogować się przez JSF lub
 *   zaimplementować osobny mechanizm autentykacji (np. JWT, OAuth2)
 * 
 * Dla pełnej obsługi REST API z Basic Auth wymagana byłaby osobna konfiguracja
 * lub użycie @BasicAuthenticationMechanismDefinition zamiast form-auth,
 * co jednak uniemożliwiłoby logowanie przez formularz JSF.
 */
@ApplicationScoped
@CustomFormAuthenticationMechanismDefinition(
        loginToContinue = @LoginToContinue(
                loginPage = "/authentication/login.xhtml",
                errorPage = "/authentication/login_error.xhtml"
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

