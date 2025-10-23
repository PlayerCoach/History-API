package pl.edu.pg.eti.kask.historyapi.servlet.note;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;
import pl.edu.pg.eti.kask.historyapi.note.service.NoteService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet("/api/notes/*")
public class NoteServlet extends HttpServlet {

    @Inject
    private NoteService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        // return all notes
        if (pathInfo == null || pathInfo.equals("/")) {
            handleGetAll(resp);

        }
        // return notes by figure id
        else if (pathInfo.startsWith("/byFigure/")) {

            handleGetByFigure(pathInfo, resp);

        }
        //return notes by user id
        else if (pathInfo.startsWith("/byUser/")) {

            handleGetByUser(pathInfo, resp);

        }
        // return single note by id
        else {
            handleGetSingle(pathInfo, resp);
        }
    }

    private void handleGetAll(HttpServletResponse resp) throws IOException {
        List<Note> notes = service.findAll();
        objectMapper.writeValue(resp.getWriter(), notes);
    }

    private void handleGetByFigure(String pathInfo, HttpServletResponse resp) throws IOException {
        try {
            UUID figureId = UUID.fromString(pathInfo.substring(10));
            List<Note> notes = service.findByFigureId(figureId);
            objectMapper.writeValue(resp.getWriter(), notes);
        } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid Figure UUID format\"}");
        }
    }

    private void handleGetByUser(String pathInfo, HttpServletResponse resp) throws IOException {
        try {

            UUID userId = UUID.fromString(pathInfo.substring(8));
            List<Note> notes = service.findByUserId(userId);
            objectMapper.writeValue(resp.getWriter(), notes);
        } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid User UUID format\"}");
        }
    }

    private void handleGetSingle(String pathInfo, HttpServletResponse resp) throws IOException {
        try {
            UUID id = UUID.fromString(pathInfo.substring(1));
            Optional<Note> noteOptional = service.findById(id);

            if (noteOptional.isPresent()) {
                objectMapper.writeValue(resp.getWriter(), noteOptional.get());
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"Note not found\"}");
            }
        } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid Note UUID format\"}");
        }
    }
}
