package pl.edu.pg.eti.kask.historyapi.historicalfigure.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.entity.HistoricalFigure;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.service.HistoricalFigureService;

import java.util.UUID;

@FacesConverter(value = "historicalFigureConverter", managed = true)
public class HistoricalFigureConverter implements Converter<HistoricalFigure> {

    @Inject
    private HistoricalFigureService figureService;

    @Override
    public HistoricalFigure getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            UUID id = UUID.fromString(value);
            return figureService.findById(id).orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, HistoricalFigure value) {
        if (value == null) {
            return "";
        }
        return value.getId().toString();
    }
}

