package pl.edu.pg.eti.kask.historyapi.historicalfigure.repository;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.entity.HistoricalFigure;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class HistoricalFigureRepository {

    private final Map<UUID, HistoricalFigure> figures = new ConcurrentHashMap<>();


    public HistoricalFigureRepository() {}

    @PostConstruct
    private void init() {
        HistoricalFigure h1 = new HistoricalFigure();
        h1.setId(UUID.fromString("15a7f9a0-7ac1-11eb-8000-0242ac110002"));
        h1.setName("Napoleon Bonaparte");
        h1.setDateOfBrith(LocalDate.of(1769, 8, 15));
        h1.setDateOfDeath(LocalDate.of(1821, 5, 5));

        HistoricalFigure h2 = new HistoricalFigure();
        h2.setId(UUID.fromString("15a7fae0-7ac1-11eb-8001-0242ac110002"));
        h2.setName("Maria Skłodowska-Curie");
        h2.setDateOfBrith(LocalDate.of(1867, 11, 7));
        h2.setDateOfDeath(LocalDate.of(1934, 7, 4));

        figures.put(h1.getId(), h1);
        figures.put(h2.getId(), h2);
    }

    public List<HistoricalFigure> findAll() {
        return new ArrayList<>(figures.values());
    }

    public Optional<HistoricalFigure> findById(UUID id) {
        return Optional.ofNullable(figures.get(id));
    }

    public void delete(UUID id) {
        figures.remove(id);
    }

    public void save(HistoricalFigure historicalFigure) {
        figures.put(historicalFigure.getId(), historicalFigure);
    }
}
