package pl.edu.pg.eti.kask.historyapi.view.exception;

import jakarta.faces.FacesException;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;

import java.util.Iterator;

/**
 * Custom ExceptionHandler do obsługi ViewExpiredException.
 * Przekierowuje użytkownika na listę notatek gdy sesja wygaśnie.
 */
public class ViewExpiredExceptionHandler extends ExceptionHandlerWrapper {

    private final ExceptionHandler wrapped;

    public ViewExpiredExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return wrapped;
    }

    @Override
    public void handle() throws FacesException {
        Iterator<ExceptionQueuedEvent> events = getUnhandledExceptionQueuedEvents().iterator();

        while (events.hasNext()) {
            ExceptionQueuedEvent event = events.next();
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
            Throwable throwable = context.getException();

            if (throwable instanceof ViewExpiredException) {
                FacesContext fc = FacesContext.getCurrentInstance();
                try {
                    NavigationHandler nav = fc.getApplication().getNavigationHandler();
                    nav.handleNavigation(fc, null, "/note/notes.xhtml?faces-redirect=true");
                    fc.renderResponse();
                } finally {
                    events.remove();
                }
            }
        }

        // Let the parent handle the rest
        getWrapped().handle();
    }
}
