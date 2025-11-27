# Laboratorium 7 - Jakarta Server Faces + CDI

## Dokumentacja implementacji

---

## 1. Konfiguracja autoryzacji (0.5 + 0.5 pkt)

### 1.1 Próba wejścia na zabezpieczony zasób przekierowuje na formularz logowania

**Implementacja:** `SecurityConfig.java` + `web.xml`

```java
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
    hashAlgorithm = Pbkdf2PasswordHash.class
)
public class SecurityConfig { }
```

**web.xml - Security Constraints:**

```xml
<security-constraint>
    <web-resource-collection>
        <web-resource-name>historical-figures</web-resource-name>
        <url-pattern>/historicalfigure/*</url-pattern>
    </web-resource-collection>
    <auth-constraint>
        <role-name>USER</role-name>
        <role-name>ADMIN</role-name>
    </auth-constraint>
</security-constraint>
```

**Dlaczego tak:**

- `@CustomFormAuthenticationMechanismDefinition` - standard Jakarta Security 3.0, integruje się z JSF
- `@DatabaseIdentityStoreDefinition` - automatyczna walidacja credentials z bazy danych
- Security constraints w `web.xml` - deklaratywne zabezpieczenie całych ścieżek URL
- Hasła hashowane PBKDF2 (210,000 iteracji) - bezpieczne przechowywanie

---

### 1.2 Tylko zalogowany użytkownik może wyświetlać listę kategorii

**Implementacja:** `web.xml`

```xml
<security-constraint>
    <web-resource-collection>
        <web-resource-name>historical-figures</web-resource-name>
        <url-pattern>/historicalfigure/*</url-pattern>
    </web-resource-collection>
    <auth-constraint>
        <role-name>USER</role-name>
        <role-name>ADMIN</role-name>
    </auth-constraint>
</security-constraint>
```

**Dlaczego tak:**

- Deklaratywne security constraint wymusza autentykację przed dostępem
- Obie role (USER i ADMIN) mają dostęp do listy
- Niezalogowany użytkownik automatycznie przekierowywany na `/authentication/login.xhtml`

---

### 1.3 Tylko administrator może (widzi przycisk) usunąć wybraną kategorię

**Implementacja:** `figures.xhtml` + `AuthView.java`

```xhtml
<h:commandButton
  value="#{msg['figures.action.delete']}"
  action="#{historicalFigureListBean.deleteFigure(figure)}"
  rendered="#{AuthView.admin}"
  styleClass="btn btn-danger btn-sm"
>
  <f:ajax render="@form" />
</h:commandButton>
```

```java
@Named
@RequestScoped
public class AuthView {
    @Inject
    private SecurityContext securityContext;

    public boolean isAdmin() {
        return securityContext.isCallerInRole("ADMIN");
    }
}
```

**Dlaczego tak:**

- `rendered="#{AuthView.admin}"` - JSF renderuje przycisk tylko dla adminów
- `SecurityContext.isCallerInRole()` - standard Jakarta Security do sprawdzania ról
- Przycisk fizycznie nie istnieje w HTML dla zwykłych użytkowników (nie tylko ukryty CSS)

---

### 1.4 Zwykły użytkownik widzi tylko swoje elementy w kategorii

**Implementacja:** `NoteListBean.java`

```java
public List<Note> getNotesForFigure() {
    if (figureId == null) return List.of();

    String username = securityContext.getCallerPrincipal().getName();
    boolean isAdmin = securityContext.isCallerInRole("ADMIN");

    if (isAdmin) {
        return noteService.findByFigureId(figureId);
    } else {
        return noteService.findByFigureIdAndOwner(figureId, username);
    }
}
```

**NoteService.java:**

```java
public List<Note> findByFigureIdAndOwner(UUID figureId, String ownerLogin) {
    return noteRepository.findByFigureIdAndOwnerLogin(figureId, ownerLogin);
}
```

**Dlaczego tak:**

- Filtrowanie na poziomie serwisu - bezpieczne, nie można obejść
- `SecurityContext` dostarcza informacje o zalogowanym użytkowniku
- Zapytanie JPA z warunkiem `WHERE createdBy.login = :login`

---

### 1.5 Administrator widzi wszystkie elementy w kategorii

**Implementacja:** Ten sam kod co powyżej

```java
if (isAdmin) {
    return noteService.findByFigureId(figureId);  // Wszystkie notatki
} else {
    return noteService.findByFigureIdAndOwner(figureId, username);  // Tylko własne
}
```

**Dlaczego tak:**

- Jeden punkt decyzyjny (`isAdmin`) kontroluje zakres danych
- Admin nie ma ograniczeń - widzi wszystko
- Logika w warstwie serwisowej, nie w widoku

---

### 1.6 Używając bezpośredniego linku do elementu tylko właściciel (lub admin) może go wyświetlić

**Implementacja:** `NoteViewBean.java`

```java
public void loadNote() {
    if (noteId == null) {
        redirectToError();
        return;
    }

    note = noteService.findById(noteId).orElse(null);

    if (note == null) {
        redirectToError();
        return;
    }

    // Sprawdzenie uprawnień
    String currentUser = securityContext.getCallerPrincipal().getName();
    boolean isAdmin = securityContext.isCallerInRole("ADMIN");
    boolean isOwner = note.getCreatedBy() != null &&
                      note.getCreatedBy().getLogin().equals(currentUser);

    if (!isAdmin && !isOwner) {
        redirectToForbidden();
    }
}
```

**Dlaczego tak:**

- Sprawdzenie przy każdym załadowaniu strony (nie tylko przy pierwszym wejściu)
- Przekierowanie na stronę błędu 403 gdy brak uprawnień
- Działa zarówno dla podglądu jak i edycji (ta sama logika)

---

### 1.7 W ramach nagłówka wyświetlenie nazwy zalogowanego użytkownika

**Implementacja:** `main.xhtml` (template)

```xhtml
<ui:fragment rendered="#{not empty AuthView.username}">
  <span class="navbar-text me-3">
    #{msg['app.header.loggedAs']}
    <strong>#{AuthView.username}</strong>
    <h:outputText
      value=" (#{msg['app.header.administrator']})"
      rendered="#{AuthView.admin}"
      styleClass="badge bg-warning ms-1"
    />
  </span>
  <h:form>
    <h:commandButton
      value="#{msg['app.header.logout']}"
      action="#{LoginView.logout}"
      styleClass="btn btn-outline-light btn-sm"
    />
  </h:form>
</ui:fragment>
```

**AuthView.java:**

```java
public String getUsername() {
    if (securityContext.getCallerPrincipal() != null) {
        return securityContext.getCallerPrincipal().getName();
    }
    return null;
}
```

**Dlaczego tak:**

- `SecurityContext.getCallerPrincipal()` - standardowy sposób pobierania użytkownika
- Badge "(Administrator)" wyświetlany tylko dla adminów
- Przycisk wylogowania wywołuje `HttpServletRequest.logout()`

---

## 2. Lokalizacja językowa aplikacji (0.5 + 0.5 pkt)

### 2.1 Konfiguracja i18n

**faces-config.xml:**

```xml
<application>
    <locale-config>
        <default-locale>pl</default-locale>
        <supported-locale>pl</supported-locale>
        <supported-locale>en</supported-locale>
    </locale-config>
    <resource-bundle>
        <base-name>bundles.messages</base-name>
        <var>msg</var>
    </resource-bundle>
</application>
```

**pom.xml:**

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-resources-plugin</artifactId>
    <version>3.3.1</version>
    <configuration>
        <encoding>UTF-8</encoding>
        <propertiesEncoding>UTF-8</propertiesEncoding>
    </configuration>
</plugin>
```

### 2.2 Pliki tłumaczeń

**messages_pl.properties:**

```properties
app.title=HistoryAPI
login.title=Logowanie
login.username=Nazwa użytkownika:
figures.list.title=Lista Postaci Historycznych
chat.title=Pokój Czatu
```

**messages_en.properties:**

```properties
app.title=HistoryAPI
login.title=Login
login.username=Username:
figures.list.title=Historical Figures List
chat.title=Chat Room
```

### 2.3 LocaleView - automatyczna detekcja języka

**LocaleView.java:**

```java
@Named
@SessionScoped
public class LocaleView implements Serializable {
    private Locale locale;

    public LocaleView() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            locale = context.getExternalContext().getRequestLocale();
            if (!isSupportedLocale(locale)) {
                locale = new Locale("pl");
            }
        }
    }

    public void changeLanguage(String language) {
        locale = new Locale(language);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
    }

    private boolean isSupportedLocale(Locale locale) {
        String lang = locale.getLanguage();
        return "pl".equals(lang) || "en".equals(lang);
    }
}
```

### 2.4 Użycie w szablonach

```xhtml
<title>#{msg['app.title']}</title>
<h2>#{msg['figures.list.title']}</h2>
<h:commandButton value="#{msg['login.submit']}" />
```

### 2.5 Dwie wersje językowe obrazka

**LocaleView.java:**

```java
public String getBackgroundImage() {
    String lang = getLanguage();
    if ("pl".equals(lang)) {
        return "background_pl.png";
    } else {
        return "background.png";
    }
}
```

**Użycie w szablonie:**

```xhtml
<h:graphicImage name="images/#{LocaleView.backgroundImage}" library="default" />
```

**Dlaczego tak:**

- `SessionScoped` - język zachowany przez całą sesję użytkownika
- Automatyczna detekcja z `Accept-Language` header przeglądarki
- Resource bundles - standard Java/JSF dla i18n
- `propertiesEncoding=UTF-8` w Maven - poprawna obsługa polskich znaków

---

## 3. Usuwanie z wykorzystaniem AJAX (0.5 + 0.5 pkt)

### 3.1 Usuwanie kategorii (postaci historycznych)

**figures.xhtml:**

```xhtml
<h:form id="figuresForm">
  <h:dataTable
    id="figuresTable"
    value="#{historicalFigureListBean.figures}"
    var="figure"
  >
    <!-- kolumny -->
    <h:column>
      <h:commandButton
        value="#{msg['figures.action.delete']}"
        action="#{historicalFigureListBean.deleteFigure(figure)}"
        rendered="#{AuthView.admin}"
        onclick="return confirm('#{msg['figures.delete.confirm']}')"
        styleClass="btn btn-danger btn-sm"
      >
        <f:ajax render="@form" />
      </h:commandButton>
    </h:column>
  </h:dataTable>
</h:form>
```

**HistoricalFigureListBean.java:**

```java
public void deleteFigure(HistoricalFigure figure) {
    figureService.delete(figure.getId());
    figures = null; // Reset cache - wymusza ponowne pobranie
}
```

### 3.2 Usuwanie elementów (notatek)

**figure.xhtml:**

```xhtml
<h:form id="notesForm">
  <h:dataTable
    id="notesTable"
    value="#{noteListBean.notesForFigure}"
    var="note"
  >
    <h:column>
      <h:commandButton
        value="#{msg['notes.action.delete']}"
        action="#{noteListBean.deleteNote(note)}"
        onclick="return confirm('#{msg['notes.delete.confirm']}')"
        styleClass="btn btn-danger btn-sm"
      >
        <f:ajax render="@form" />
      </h:commandButton>
    </h:column>
  </h:dataTable>
</h:form>
```

**Dlaczego tak:**

- `<f:ajax render="@form">` - tylko formularz jest aktualizowany, nie cała strona
- Mniejszy transfer danych, szybsza odpowiedź
- `onclick="return confirm(...)"` - potwierdzenie przed usunięciem
- Płynne UX bez przeładowania strony

---

## 4. Interceptor logujący operacje (0.5 + 0.5 pkt)

### 4.1 Adnotacja @Logged

**Logged.java:**

```java
@InterceptorBinding
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Logged {
}
```

### 4.2 LoggingInterceptor

**LoggingInterceptor.java:**

```java
@Logged
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class LoggingInterceptor {

    private static final Logger LOGGER = Logger.getLogger(LoggingInterceptor.class.getName());

    @Inject
    private SecurityContext securityContext;

    @AroundInvoke
    public Object logOperation(InvocationContext context) throws Exception {
        String methodName = context.getMethod().getName();
        String username = getUsername();
        Object[] parameters = context.getParameters();

        // Przed wykonaniem
        if (methodName.equals("save") || methodName.equals("update")) {
            UUID resourceId = extractResourceId(parameters);
            LOGGER.info(String.format("User '%s' is performing operation: %s on resource ID: %s",
                    username, methodName.toUpperCase(), resourceId));
        } else if (methodName.equals("delete")) {
            UUID resourceId = extractResourceId(parameters);
            LOGGER.info(String.format("User '%s' is performing operation: DELETE on resource ID: %s",
                    username, resourceId));
        }

        // Wykonanie metody
        Object result = context.proceed();

        // Po wykonaniu
        LOGGER.info(String.format("User '%s' successfully completed operation: %s",
                username, methodName.toUpperCase()));

        return result;
    }

    private String getUsername() {
        if (securityContext != null && securityContext.getCallerPrincipal() != null) {
            return securityContext.getCallerPrincipal().getName();
        }
        return "UNKNOWN";
    }

    private UUID extractResourceId(Object[] parameters) {
        if (parameters != null && parameters.length > 0) {
            Object param = parameters[0];
            if (param instanceof UUID) {
                return (UUID) param;
            }
            // Refleksja dla encji z metodą getId()
            try {
                Method getIdMethod = param.getClass().getMethod("getId");
                Object id = getIdMethod.invoke(param);
                if (id instanceof UUID) {
                    return (UUID) id;
                }
            } catch (Exception e) { }
        }
        return null;
    }
}
```

### 4.3 Rejestracja w beans.xml

**beans.xml:**

```xml
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee" version="4.0" bean-discovery-mode="all">
    <interceptors>
        <class>pl.edu.pg.eti.kask.historyapi.interceptor.LoggingInterceptor</class>
    </interceptors>
</beans>
```

### 4.4 Użycie na serwisie

**NoteService.java:**

```java
@Logged
public void save(Note note) {
    noteRepository.save(note);
}

@Logged
public void update(Note note) {
    noteRepository.save(note);
}

@Logged
public void delete(UUID id) {
    noteRepository.delete(id);
}
```

### 4.5 Przykładowe logi

```
INFO: User 'john' is performing operation: SAVE on resource ID: 123e4567-e89b-12d3-a456-426614174000
INFO: User 'john' successfully completed operation: SAVE
INFO: User 'admin' is performing operation: DELETE on resource ID: 987fcdeb-51a2-3bc4-d567-890123456789
INFO: User 'admin' successfully completed operation: DELETE
```

**Dlaczego tak:**

- **AOP (Aspect-Oriented Programming)** - logika logowania oddzielona od biznesowej
- **@InterceptorBinding** - standard CDI, czysta deklaratywna konfiguracja
- **SecurityContext** - automatyczne pobieranie nazwy użytkownika
- **Refleksja** - uniwersalne wyciąganie ID z różnych typów encji
- **Zero boilerplate** - wystarczy `@Logged` na metodzie

---

## 5. Implementacja chatu (1 + 1 pkt)

### 5.1 Architektura

```
┌─────────────┐     AJAX POST      ┌─────────────┐
│   Browser   │ ─────────────────► │  ChatView   │
│  (chat.xhtml)                    │ (JSF Bean)  │
└─────────────┘                    └──────┬──────┘
       ▲                                  │
       │                                  │ CDI Event
       │ WebSocket                        ▼
       │                           ┌─────────────────┐
       │                           │ChatMessageEvent │
       │                           └────────┬────────┘
       │                                    │ @Observes
       │                                    ▼
       │                           ┌─────────────────┐
       └────────────────────────── │ ChatWebSocket   │
              push message         │ (@ServerEndpoint)
                                   └─────────────────┘
```

### 5.2 ChatMessage DTO

**ChatMessage.java:**

```java
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ChatMessage implements Serializable {
    private String sender;
    private String recipient;
    private String content;

    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private boolean privateMessage;

    public boolean isFor(String username) {
        if (!privateMessage) return true;
        return username.equals(recipient) || username.equals(sender);
    }
}
```

### 5.3 CDI Event

**ChatMessageEvent.java:**

```java
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ChatMessageEvent implements Serializable {
    private ChatMessage message;
}
```

### 5.4 ChatView (wysyłanie przez AJAX)

**ChatView.java:**

```java
@Named
@SessionScoped
public class ChatView implements Serializable {

    @Inject
    private SecurityContext securityContext;

    @Inject
    private Event<ChatMessageEvent> chatMessageEvent;

    @Inject
    private UserService userService;

    @Getter @Setter
    private String messageContent;

    @Getter @Setter
    private String selectedRecipient;

    public String getCurrentUsername() {
        return securityContext.getCallerPrincipal().getName();
    }

    public void sendMessage() {
        String sender = getCurrentUsername();

        if (messageContent == null || messageContent.trim().isEmpty()) {
            return;
        }

        boolean isPrivate = selectedRecipient != null &&
                           !selectedRecipient.isEmpty() &&
                           !"ALL".equals(selectedRecipient);

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .recipient(isPrivate ? selectedRecipient : null)
                .content(messageContent.trim())
                .timestamp(LocalDateTime.now())
                .privateMessage(isPrivate)
                .build();

        // Wystrzelenie CDI Event
        chatMessageEvent.fire(new ChatMessageEvent(message));

        messageContent = "";  // Wyczyść pole
    }

    public List<User> getAllUsers() {
        String currentUser = getCurrentUsername();
        return userService.findAll().stream()
                .filter(user -> !user.getLogin().equals(currentUser))
                .collect(Collectors.toList());
    }
}
```

### 5.5 ChatWebSocket (odbieranie przez WebSocket)

**ChatWebSocket.java:**

```java
@ApplicationScoped
@ServerEndpoint("/chat-websocket")
public class ChatWebSocket {

    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Jsonb jsonb = JsonbBuilder.create();

    @OnOpen
    public void onOpen(Session session) {
        String username = getUsernameFromSession(session);
        if (username != null) {
            sessions.put(username, session);
            broadcastUserList();
        }
    }

    @OnClose
    public void onClose(Session session) {
        String username = getUsernameFromSession(session);
        if (username != null) {
            sessions.remove(username);
            broadcastUserList();
        }
    }

    // Obserwator CDI Event
    public void onChatMessage(@Observes ChatMessageEvent event) {
        ChatMessage message = event.getMessage();
        String jsonMessage = jsonb.toJson(message);

        if (message.isPrivateMessage()) {
            // Wiadomość prywatna - tylko do nadawcy i odbiorcy
            sendToUser(message.getSender(), jsonMessage);
            sendToUser(message.getRecipient(), jsonMessage);
        } else {
            // Wiadomość publiczna - broadcast
            broadcast(jsonMessage);
        }
    }

    private void broadcast(String message) {
        sessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (IOException e) { }
        });
    }

    private void sendToUser(String username, String message) {
        Session session = sessions.get(username);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) { }
        }
    }

    private void broadcastUserList() {
        String userListJson = jsonb.toJson(Map.of(
            "type", "userList",
            "users", sessions.keySet()
        ));
        broadcast(userListJson);
    }

    private String getUsernameFromSession(Session session) {
        String query = session.getQueryString();
        if (query != null && query.contains("username=")) {
            return query.replace("username=", "");
        }
        return null;
    }

    public static Set<String> getOnlineUsers() {
        return sessions.keySet();
    }
}
```

### 5.6 Widok chat.xhtml

**chat.xhtml:**

```xhtml
<ui:composition template="/WEB-INF/template/main.xhtml">
  <ui:define name="content">
    <h2>#{msg['chat.title']}</h2>

    <!-- Kontener wiadomości -->
    <div id="chatMessages" style="height: 400px; overflow-y: auto;">
      <p class="text-muted">#{msg['chat.noMessages']}</p>
    </div>

    <!-- Formularz wysyłania (AJAX) -->
    <h:form id="chatForm">
      <div class="row">
        <div class="col-md-3">
          <h:selectOneMenu
            value="#{ChatView.selectedRecipient}"
            styleClass="form-select"
          >
            <f:selectItem itemValue="ALL" itemLabel="#{msg['chat.everyone']}" />
            <f:selectItems
              value="#{ChatView.allUsers}"
              var="user"
              itemValue="#{user.login}"
              itemLabel="#{user.login}"
            />
          </h:selectOneMenu>
        </div>
        <div class="col-md-7">
          <h:inputText
            value="#{ChatView.messageContent}"
            styleClass="form-control"
          />
        </div>
        <div class="col-md-2">
          <h:commandButton
            value="#{msg['chat.send']}"
            action="#{ChatView.sendMessage}"
          >
            <f:ajax execute="@form" render="@form" />
          </h:commandButton>
        </div>
      </div>
    </h:form>

    <!-- Lista użytkowników online -->
    <div id="onlineUsersList"></div>

    <!-- JavaScript WebSocket -->
    <script>
      var chatSocket;
      var currentUsername = "#{ChatView.currentUsername}";

      document.addEventListener("DOMContentLoaded", function () {
        connectWebSocket();
      });

      function connectWebSocket() {
        var wsUrl =
          "ws://" +
          window.location.host +
          "#{request.contextPath}/chat-websocket?username=" +
          currentUsername;

        chatSocket = new WebSocket(wsUrl);

        chatSocket.onmessage = function (event) {
          var data = JSON.parse(event.data);
          if (data.type === "userList") {
            updateOnlineUsers(data.users);
          } else {
            addChatMessage(data);
          }
        };

        chatSocket.onclose = function () {
          setTimeout(connectWebSocket, 5000); // Reconnect
        };
      }

      function addChatMessage(message) {
        var container = document.getElementById("chatMessages");
        var div = document.createElement("div");
        div.className = message.privateMessage ? "bg-warning" : "bg-light";
        div.innerHTML =
          "<strong>" + message.sender + "</strong>: " + message.content;
        container.appendChild(div);
        container.scrollTop = container.scrollHeight;
      }

      function updateOnlineUsers(users) {
        var list = document.getElementById("onlineUsersList");
        list.innerHTML = users
          .map((u) => '<span class="badge bg-success">' + u + "</span>")
          .join(" ");
      }
    </script>
  </ui:define>
</ui:composition>
```

### 5.7 Konfiguracja WebSocket w server.xml

```xml
<featureManager>
    <feature>websocket-2.1</feature>
</featureManager>
```

**Dlaczego tak:**

1. **Hybrydowe podejście AJAX + WebSocket:**

   - AJAX do wysyłania - integracja z JSF, walidacja, security context
   - WebSocket do odbierania - real-time push, niskie opóźnienie

2. **CDI Event (`@Observes`):**

   - Luźne sprzężenie - ChatView nie zna ChatWebSocket
   - Łatwe testowanie komponentów osobno
   - Możliwość dodania innych obserwatorów (np. logging)

3. **Wiadomości prywatne:**

   - `privateMessage` flag w DTO
   - Selektywne wysyłanie tylko do nadawcy i odbiorcy
   - Wizualne oznaczenie (żółte tło)

4. **Lista użytkowników online:**

   - `ConcurrentHashMap` - thread-safe
   - Automatyczna aktualizacja przy connect/disconnect
   - Broadcast nowej listy do wszystkich

5. **Reconnect:**
   - Automatyczne ponowne połączenie po 5 sekundach
   - Status połączenia wyświetlany użytkownikowi

---

## Podsumowanie technologii

| Funkcjonalność    | Technologia                         | Standard                 |
| ----------------- | ----------------------------------- | ------------------------ |
| Autentykacja      | Form Auth + Database Identity Store | Jakarta Security 3.0     |
| Autoryzacja       | @RolesAllowed, SecurityContext      | Jakarta Security 3.0     |
| Widoki            | JSF + Facelets                      | Jakarta Faces 4.0        |
| i18n              | Resource Bundles                    | Java SE                  |
| AJAX              | f:ajax                              | Jakarta Faces 4.0        |
| Interceptory      | @Interceptor, @AroundInvoke         | CDI 4.0                  |
| Eventy            | Event<T>, @Observes                 | CDI 4.0                  |
| WebSocket         | @ServerEndpoint                     | Jakarta WebSocket 2.1    |
| Serializacja JSON | JSON-B                              | Jakarta JSON Binding 3.0 |

---

## Autor

Olaf Jedliński - s193415

