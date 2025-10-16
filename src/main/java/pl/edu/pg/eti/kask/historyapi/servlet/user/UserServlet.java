package pl.edu.pg.eti.kask.historyapi.servlet.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.edu.pg.eti.kask.historyapi.user.entity.User;
import pl.edu.pg.eti.kask.historyapi.user.service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet("/api/users/*")
public class UserServlet extends HttpServlet {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init() {
        // fix  500 error with LocalDate.
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        // Case 1: Request for all users (e.g., GET /api/users/)
        if (pathInfo == null || pathInfo.equals("/")) {
            handleGetAllUsers(resp);
            return;
        }

        // Case 2: Request for a single user (e.g., GET /api/users/{uuid})
        handleGetSingleUser(pathInfo, resp);
    }

    private void handleGetAllUsers(HttpServletResponse resp) throws IOException {
        List<User> users = UserService.findAll();
        objectMapper.writeValue(resp.getWriter(), users);
    }

    private void handleGetSingleUser(String pathInfo, HttpServletResponse resp) throws IOException {
        try {
            UUID userId = UUID.fromString(pathInfo.substring(1));
            Optional<User> userOptional = UserService.findById(userId);

            if (userOptional.isPresent()) {
                // Use objectMapper to write the single user object as JSON
                objectMapper.writeValue(resp.getWriter(), userOptional.get());
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"User not found\"}");
            }
        } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid UUID format\"}");
        }
    }
}