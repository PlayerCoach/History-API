package pl.edu.pg.eti.kask.historyapi.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

@ApplicationScoped
@BasicAuthenticationMechanismDefinition(realmName = "history-api-realm")
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

