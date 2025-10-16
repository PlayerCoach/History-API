package pl.edu.pg.eti.kask.historyapi.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import pl.edu.pg.eti.kask.historyapi.user.avatar.AvatarService;

import java.io.*;
import java.util.UUID;

@WebServlet("/api/avatar/*")
@MultipartConfig
public class AvatarServlet extends HttpServlet {

    private AvatarService avatarService;

    @Override
    public void init() {
        String dir = getServletContext().getInitParameter("avatar.storage.dir");
        if (dir == null || dir.isBlank()) {
            dir = "src/main/webapp/avatars"; // fallback if not specified
        }
        avatarService = new AvatarService(dir);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        UUID id = extractUserId(req.getPathInfo(), resp);
        if (id == null) return;

        Part part = req.getPart("avatar");
        if (part == null || part.getSize() == 0) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"No avatar provided\"}");
            return;
        }

        avatarService.save(id + ".jpg", part.getInputStream());
        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().write("{\"message\": \"Avatar uploaded successfully\"}");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UUID id = extractUserId(req.getPathInfo(), resp);
        if (id == null) return;

        File avatar = avatarService.get(id + ".jpg");
        if (avatar == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\": \"Avatar not found\"}");
            return;
        }

        resp.setContentType("image/jpeg");
        try (FileInputStream in = new FileInputStream(avatar);
             OutputStream out = resp.getOutputStream()) {
            in.transferTo(out);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UUID id = extractUserId(req.getPathInfo(), resp);
        if (id == null) return;

        if (avatarService.delete(id + ".jpg")) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"Avatar deleted\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\": \"Avatar not found\"}");
        }
    }

    private UUID extractUserId(String path, HttpServletResponse resp) throws IOException {
        if (path == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid path format\"}");
            return null;
        }
        try {
            return UUID.fromString(path.split("/")[1]);
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid UUID\"}");
            return null;
        }
    }
}
