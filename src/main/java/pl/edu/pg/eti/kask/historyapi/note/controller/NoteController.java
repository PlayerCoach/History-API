package pl.edu.pg.eti.kask.historyapi.note.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.service.HistoricalFigureService;
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;
import pl.edu.pg.eti.kask.historyapi.note.service.NoteService;

import java.net.URI;
import java.util.UUID;

@Path("/figures/{figureId}/notes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NoteController {

    @Inject
    private NoteService noteService;

    @Inject
    private HistoricalFigureService figureService;

    @GET
    @Path("/")
    public Response getNotesForFigure(@PathParam("figureId") UUID figureId) {
        if(noteService.findByFigureId(figureId).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(noteService.findByFigureId(figureId)).build();
    }

    @GET
    @Path("/{noteId}")
    public Response getNoteById(@PathParam("figureId") UUID figureId,
                                @PathParam("noteId") UUID noteId) {
        return noteService.findById(noteId)
                .filter(note -> note.getHistoricalFigure() != null &&
                               note.getHistoricalFigure().getId().equals(figureId))
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND)).build();
    }

    @POST
    @Path("/")
    public Response createNoteForFigure(@PathParam("figureId") UUID figureId, Note note) {
        return figureService.findById(figureId)
                .map(figure -> {
                    note.setHistoricalFigure(figure);
                    note.setId(UUID.randomUUID());
                    noteService.save(note);
                    URI location = UriBuilder.fromPath("/figures/{figureId}/notes/{noteId}")
                            .build(figureId, note.getId());
                    return Response.created(location).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Path("/{noteId}")
    public Response updateNote(@PathParam("figureId") UUID figureId,
                               @PathParam("noteId") UUID noteId,
                               Note note) {
        return figureService.findById(figureId)
                .map(figure -> {
                    note.setId(noteId);
                    note.setHistoricalFigure(figure);
                    noteService.save(note);
                    return Response.ok(note).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{noteId}")
    public Response deleteHierarchicalNote(@PathParam("noteId") UUID noteId) {
        noteService.delete(noteId);
        return Response.noContent().build(); // 204
    }
}