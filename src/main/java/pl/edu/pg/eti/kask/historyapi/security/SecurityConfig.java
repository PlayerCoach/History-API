package pl.edu.pg.eti.kask.historyapi.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

/**
 * Configuration class for security context.
 * Uses CustomFormAuthenticationMechanismDefinition for JSF pages.
 * REST API endpoints will use Basic Auth via custom filter/interceptor.
 */
@ApplicationScoped
// BasicAuth is commented out - we'll handle it separately for REST endpoints
//@BasicAuthenticationMechanismDefinition(realmName = "history-api-realm")
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

