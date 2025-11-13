package pl.edu.pg.eti.kask.historyapi.note.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pl.edu.pg.eti.kask.historyapi.note.service.NoteService;

import java.util.UUID;

@Path("/notes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NoteSimpleController {
    @Inject
    private NoteService noteService;

    @GET
    @Path("/")
    public Response getAllNotes() {
        if (noteService.findAll().isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(noteService.findAll()).build();
    }

    @GET
    @Path("/{noteId}")
    public Response getNoteById(@PathParam("noteId") UUID noteId) {
        return noteService.findById(noteId)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND)).build();
    }

    @DELETE
    @Path("/{noteId}")
    public Response deleteNoteById(@PathParam("noteId") UUID noteId) {
        noteService.delete(noteId);
        return Response.noContent().build(); // 204
    }
}
