package pl.edu.pg.eti.kask.historyapi.security;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.credential.Password;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;
import java.util.Set;

/**
 * Filter to handle Basic Authentication for REST API endpoints.
 * This allows REST clients to authenticate using Authorization header
 * while JSF pages use form-based authentication.
 *
 * Note: This filter requires proper configuration of DatabaseIdentityStore
 * in SecurityConfig to work correctly.
 */
@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION)
public class BasicAuthFilter implements ContainerRequestFilter {

    @Inject
    private IdentityStoreHandler identityStoreHandler;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Only apply to REST API paths
        String path = requestContext.getUriInfo().getPath();
        if (!path.startsWith("api/")) {
            return;
        }

        String authorizationHeader = requestContext.getHeaderString("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Basic ")) {
            String base64Credentials = authorizationHeader.substring("Basic ".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);

            String[] values = credentials.split(":", 2);
            if (values.length == 2) {
                String username = values[0];
                String password = values[1];

                try {
                    UsernamePasswordCredential credential = new UsernamePasswordCredential(username, new Password(password));
                    CredentialValidationResult result = identityStoreHandler.validate(credential);

                    if (result.getStatus() == CredentialValidationResult.Status.VALID) {
                        final String user = result.getCallerPrincipal().getName();
                        final Set<String> roles = result.getCallerGroups();

                        requestContext.setSecurityContext(new SecurityContext() {
                            @Override
                            public Principal getUserPrincipal() {
                                return () -> user;
                            }

                            @Override
                            public boolean isUserInRole(String role) {
                                return roles.contains(role);
                            }

                            @Override
                            public boolean isSecure() {
                                return requestContext.getSecurityContext().isSecure();
                            }

                            @Override
                            public String getAuthenticationScheme() {
                                return "BASIC";
                            }
                        });
                        return;
                    }
                } catch (Exception e) {
                    // Validation failed - let security constraints handle it
                }
            }
        }

        // If we reach here and path requires auth, security constraints will handle it
    }
}

