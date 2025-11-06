package pl.edu.pg.eti.kask.historyapi.note.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;
import pl.edu.pg.eti.kask.historyapi.note.service.NoteService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NoteController {

    @Inject
    private NoteService noteService;
    @GET
    @Path("/figures/{figureId}/notes")
    public List<Note> getNotesForFigure(@PathParam("figureId") UUID figureId) {
        return noteService.findByFigureId(figureId);
    }
    @GET
    @Path("/figures/{figureId}/notes/{noteId}")
    public Response getNoteById(@PathParam("figureId") UUID figureId,
                                @PathParam("noteId") UUID noteId) {
        return noteService.findById(noteId)
                .filter(note -> note.getHistoricalFigureId().equals(figureId))
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND)).build();
    }
    @POST
    @Path("/figures/{figureId}/notes")
    public Response createNoteForFigure(@PathParam("figureId") UUID figureId, Note note) {
        note.setHistoricalFigureId(figureId);
        note.setId(UUID.randomUUID());

        noteService.save(note);

        URI location = UriBuilder.fromPath("/figures/{figureId}/notes/{noteId}")
                .build(figureId, note.getId());
        return Response.created(location).build(); // 201
    }

    @PUT
    @Path("/figures/{figureId}/notes/{noteId}")
    public Response updateNote(@PathParam("figureId") UUID figureId,
                               @PathParam("noteId") UUID noteId,
                               Note note) {

        note.setId(noteId);
        note.setHistoricalFigureId(figureId);
        noteService.save(note);
        return Response.ok(note).build(); // 200
    }

    @DELETE
    @Path("/figures/{figureId}/notes/{noteId}")
    public Response deleteHierarchicalNote(@PathParam("noteId") UUID noteId) {
        noteService.delete(noteId);
        return Response.noContent().build(); // 204
    }

    @GET
    @Path("/notes")
    public List<Note> getAllNotes() {
        return noteService.findAll();
    }


    @GET
    @Path("/notes/{id}")
    public Response getFlatNoteById(@PathParam("id") UUID noteId) {
        return noteService.findById(noteId)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND)).build();
    }
    @DELETE
    @Path("/notes/{id}")
    public Response deleteFlatNote(@PathParam("id") UUID noteId) {
        noteService.delete(noteId);
        return Response.noContent().build(); // 204
    }
}