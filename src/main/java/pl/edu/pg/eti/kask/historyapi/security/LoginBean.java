package pl.edu.pg.eti.kask.historyapi.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.Password;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Named
@RequestScoped
public class LoginBean implements Serializable {

    @Inject
    private SecurityContext securityContext;

    @Inject
    private FacesContext facesContext;

    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private String password;

    public String login() {
        Credential credential = new UsernamePasswordCredential(username, new Password(password));
        AuthenticationStatus status = securityContext.authenticate(
                (HttpServletRequest) facesContext.getExternalContext().getRequest(),
                (HttpServletResponse) facesContext.getExternalContext().getResponse(),
                jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters.withParams().credential(credential)
        );

        if (status == AuthenticationStatus.SEND_CONTINUE) {
            facesContext.responseComplete();
            return null;
        } else if (status == AuthenticationStatus.SEND_FAILURE) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Błąd logowania", "Nieprawidłowa nazwa użytkownika lub hasło"));
            return null;
        }

        return "/index.xhtml?faces-redirect=true";
    }

    public String logout() {
        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        try {
            request.logout();
            facesContext.getExternalContext().invalidateSession();
        } catch (ServletException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Błąd", "Nie udało się wylogować"));
            e.printStackTrace();
        }
        return "/index.xhtml?faces-redirect=true";
    }
}

