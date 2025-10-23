package pl.edu.pg.eti.kask.historyapi.servlet.historicalfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.entity.HistoricalFigure;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.service.HistoricalFigureService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet("/api/figures/*")
public class HistoricalFigureServlet extends HttpServlet {

    @Inject
    private HistoricalFigureService service;

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

        if (pathInfo == null || pathInfo.equals("/")) {
            handleGetAll(resp);
        } else {
            handleGetSingle(pathInfo, resp);
        }
    }

    private void handleGetAll(HttpServletResponse resp) throws IOException {
        List<HistoricalFigure> figures = service.findAll();
        objectMapper.writeValue(resp.getWriter(), figures);
    }

    private void handleGetSingle(String pathInfo, HttpServletResponse resp) throws IOException {
        try {
            UUID id = UUID.fromString(pathInfo.substring(1));
            Optional<HistoricalFigure> figureOptional = service.findById(id);

            if (figureOptional.isPresent()) {
                objectMapper.writeValue(resp.getWriter(), figureOptional.get());
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"HistoricalFigure not found\"}");
            }
        } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid UUID format\"}");
        }
    }
}
