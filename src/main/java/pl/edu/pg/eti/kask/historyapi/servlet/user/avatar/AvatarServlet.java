package pl.edu.pg.eti.kask.historyapi.servlet.user.avatar;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import pl.edu.pg.eti.kask.historyapi.user.avatar.repository.AvatarRepository;
import pl.edu.pg.eti.kask.historyapi.user.avatar.service.AvatarService;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet("/api/avatars/*")
@MultipartConfig(
        maxFileSize = 1024 * 1024 * 10 // 10 MB
)
public class AvatarServlet extends HttpServlet {

    private AvatarService avatarService;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png");

    @Override
    public void init() throws ServletException {
        String dir = getServletContext().getInitParameter("avatar.storage.dir");
        if (dir == null || dir.isBlank()) {
            throw new ServletException("Parameter 'avatar.storage.dir' not configured in web.xml");
        }

        AvatarRepository avatarRepository = new AvatarRepository(dir);
        this.avatarService = new AvatarService(avatarRepository);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UUID userId = extractUserId(req.getPathInfo(), resp);
        if (userId == null) return;

        Part part = req.getPart("avatar");
        if (part == null || part.getSize() == 0) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"No avatar file provided in 'avatar' part\"}");
            return;
        }

        String extension = getFileExtension(part.getSubmittedFileName());

        // Check for allowed file types
        if (extension.isEmpty() || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid file type. Allowed: " + ALLOWED_EXTENSIONS + "\"}");
            return;
        }

        try {
            avatarService.save(userId, extension, part.getInputStream());
            resp.setStatus(HttpServletResponse.SC_CREATED); // 201 Created
            resp.getWriter().write("{\"message\": \"Avatar uploaded successfully\"}");
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UUID userId = extractUserId(req.getPathInfo(), resp);
        if (userId == null) return;

        Optional<File> avatarFile = avatarService.find(userId);

        if (avatarFile.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\": \"Avatar not found for this user\"}");
            return;
        }

        File avatar = avatarFile.get();
        // Dynamically set the content type based on the file's extension.
        resp.setContentType(getServletContext().getMimeType(avatar.getName()));
        resp.setContentLengthLong(avatar.length());

        try (FileInputStream in = new FileInputStream(avatar);
             OutputStream out = resp.getOutputStream()) {
            in.transferTo(out);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UUID userId = extractUserId(req.getPathInfo(), resp);
        if (userId == null) return;

        if (avatarService.delete(userId)) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"Avatar deleted successfully\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\": \"Avatar not found for this user\"}");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex > 0) ? fileName.substring(dotIndex) : "";
    }

    private UUID extractUserId(String pathInfo, HttpServletResponse resp) throws IOException {
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"User ID is missing in the URL path\"}");
            return null;
        }
        try {
            // Assumes path is like "/{uuid}"
            return UUID.fromString(pathInfo.substring(1));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid UUID format in the URL path\"}");
            return null;
        }
    }
}