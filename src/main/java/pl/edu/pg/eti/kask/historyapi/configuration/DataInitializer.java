package pl.edu.pg.eti.kask.historyapi.configuration;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.entity.HistoricalFigure;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.service.HistoricalFigureService;
import pl.edu.pg.eti.kask.historyapi.note.entity.Mode;
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;
import pl.edu.pg.eti.kask.historyapi.note.service.NoteService;

import java.time.LocalDate;
import java.util.UUID;

@Singleton
@Startup
public class DataInitializer {

    @Inject
    private HistoricalFigureService figureService;

    @Inject
    private NoteService noteService;

    @PostConstruct
    private void init() {
        // Create Historical Figures
        HistoricalFigure napoleon = new HistoricalFigure();
        napoleon.setId(UUID.fromString("15a7f9a0-7ac1-11eb-8000-0242ac110002"));
        napoleon.setName("Napoleon Bonaparte");
        napoleon.setDateOfBrith(LocalDate.of(1769, 8, 15));
        napoleon.setDateOfDeath(LocalDate.of(1821, 5, 5));

        HistoricalFigure curie = new HistoricalFigure();
        curie.setId(UUID.fromString("15a7fae0-7ac1-11eb-8001-0242ac110002"));
        curie.setName("Maria Skłodowska-Curie");
        curie.setDateOfBrith(LocalDate.of(1867, 11, 7));
        curie.setDateOfDeath(LocalDate.of(1934, 7, 4));

        figureService.save(napoleon);
        figureService.save(curie);

        // Create Notes
        UUID testUserId = UUID.fromString("fe003ce8-0dae-46cb-8d01-104d1d91d4a0");

        Note n1 = new Note();
        n1.setId(UUID.fromString("25b8c1f0-7ac1-11eb-8000-0242ac110002"));
        n1.setTitle("Bitwa pod Waterloo");
        n1.setContent("Ostateczna porażka Napoleona.");
        n1.setMode(Mode.PUBLIC);
        n1.setHistoricalFigure(napoleon);
        n1.setUserId(testUserId);

        Note n2 = new Note();
        n2.setId(UUID.randomUUID());
        n2.setTitle("Odkrycie Polonu");
        n2.setContent("Pierwiastek nazwany na cześć Polski.");
        n2.setMode(Mode.PUBLIC);
        n2.setHistoricalFigure(curie);
        n2.setUserId(testUserId);

        Note n3 = new Note();
        n3.setId(UUID.randomUUID());
        n3.setTitle("Odkrycie Radu");
        n3.setContent("Kolejne wielkie odkrycie.");
        n3.setMode(Mode.PRIVATE);
        n3.setHistoricalFigure(curie);
        n3.setUserId(testUserId);

        noteService.save(n1);
        noteService.save(n2);
        noteService.save(n3);
    }
}

