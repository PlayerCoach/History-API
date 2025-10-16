package pl.edu.pg.eti.kask.historyapi.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.edu.pg.eti.kask.historyapi.repository.Repository;

import java.io.IOException;
import java.util.UUID;

@WebServlet("/api/users/*")
public class UserServlet extends HttpServlet {
    private final Repository repo = new Repository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getPathInfo();

        if (path == null || path.equals("/")) {
            for (var user : repo.getAllUsers()) {
                String userJson = user.ToJsonString();
                resp.getWriter().println(userJson);
            }
            return;
        }

        try {
            UUID id = UUID.fromString(path.substring(1));
            repo.getUser(id)
                    .ifPresentOrElse(
                            user -> {
                                try {
                                    resp.getWriter().println(user.ToJsonString());
                                } catch (IOException e)
                                {
                                    throw new RuntimeException(e);
                                }
                            },
                            () -> resp.setStatus(HttpServletResponse.SC_NOT_FOUND)
                    );
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}