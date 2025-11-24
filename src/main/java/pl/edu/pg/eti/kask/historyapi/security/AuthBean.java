package pl.edu.pg.eti.kask.historyapi.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.SecurityContext;
import lombok.Getter;

import java.io.Serializable;
import java.security.Principal;

@Named
@RequestScoped
public class AuthBean implements Serializable {

    @Inject
    private SecurityContext securityContext;

    @Getter
    private Principal principal;

    public String getUsername() {
        Principal principal = securityContext.getCallerPrincipal();
        return principal != null ? principal.getName() : null;
    }

    public boolean isLoggedIn() {
        return securityContext.getCallerPrincipal() != null;
    }

    public boolean isAdmin() {
        return securityContext.isCallerInRole("ADMIN");
    }

    public boolean isUser() {
        return securityContext.isCallerInRole("USER");
    }

    public boolean hasRole(String role) {
        return securityContext.isCallerInRole(role);
    }
}

