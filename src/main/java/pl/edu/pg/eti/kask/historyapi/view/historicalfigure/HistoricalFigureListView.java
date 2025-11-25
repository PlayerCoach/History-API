package pl.edu.pg.eti.kask.historyapi.view.historicalfigure;

import pl.edu.pg.eti.kask.historyapi.historicalfigure.entity.HistoricalFigure;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.service.HistoricalFigureService;

import java.util.List;
import java.util.UUID;

@Named("figureListView")
@RequestScoped
public class HistoricalFigureListView {

    @Inject
    private HistoricalFigureService figureService;

    private List<HistoricalFigure> figures;


    public List<HistoricalFigure> getFigures() {
        if (figures == null) {
            figures = figureService.findAll();
        }
        return figures;
    }

    public String deleteFigure(UUID id) {
        figureService.delete(id);
        // Reset listy aby AJAX przeładował dane
        figures = null;
        return null; // Dla AJAX zwracamy null zamiast nawigacji
    }
}
