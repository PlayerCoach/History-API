package pl.edu.pg.eti.kask.historyapi.historicalfigure.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.entity.HistoricalFigure;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.repository.HistoricalFigureRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class HistoricalFigureService {

    @Inject
    private HistoricalFigureRepository repository;

    public HistoricalFigureService() {}

    public List<HistoricalFigure> findAll() {
        return repository.findAll();
    }

    public Optional<HistoricalFigure> findById(UUID id) {
        return repository.findById(id);
    }
}