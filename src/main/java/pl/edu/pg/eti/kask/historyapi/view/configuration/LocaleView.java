package pl.edu.pg.eti.kask.historyapi.view.configuration;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import lombok.Setter;

import java.io.Serializable;
import java.util.Locale;

/**
 * JSF View do obsługi lokalizacji językowej.
 */
@Named
@SessionScoped
public class LocaleView implements Serializable {

    @Setter
    private Locale locale;

    public LocaleView() {
        initLocale();
    }

    private void initLocale() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null && context.getExternalContext() != null) {
            locale = context.getExternalContext().getRequestLocale();
            if (!isSupportedLocale(locale)) {
                locale = new Locale("pl");
            }
        } else {
            locale = new Locale("pl");
        }
    }

    public Locale getLocale() {
        if (locale == null) {
            initLocale();
        }
        return locale;
    }

    public String getLanguage() {
        if (locale == null) {
            initLocale();
        }
        return locale.getLanguage();
    }

    public void changeLanguage(String language) {
        locale = new Locale(language);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
    }

    public String getBackgroundImage() {
        String lang = getLanguage();
        if ("pl".equals(lang)) {
            return "background_pl.png";
        } else {
            return "background.png";
        }
    }

    private boolean isSupportedLocale(Locale locale) {
        if (locale == null) {
            return false;
        }
        String lang = locale.getLanguage();
        return "pl".equals(lang) || "en".equals(lang);
    }
}
