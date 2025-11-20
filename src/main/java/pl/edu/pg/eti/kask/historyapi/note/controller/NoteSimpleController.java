package pl.edu.pg.eti.kask.historyapi.note.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;
import pl.edu.pg.eti.kask.historyapi.note.service.NoteService;

import java.util.List;
import java.util.UUID;

@Path("/notes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NoteSimpleController {

    @Inject
    private NoteService noteService;

    @Context
    private SecurityContext securityContext;

    @GET
    @Path("/")
    @RolesAllowed({"ADMIN", "USER"})
    public Response getAllNotes() {
        String username = securityContext.getUserPrincipal().getName();
        boolean isAdmin = securityContext.isUserInRole("ADMIN");

        List<Note> notes;
        if (isAdmin) {
            notes = noteService.findAll();
        } else {
            notes = noteService.findByOwner(username);
        }

        if (notes.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(notes).build();
    }

    @GET
    @Path("/{noteId}")
    @RolesAllowed({"ADMIN", "USER"})
    public Response getNoteById(@PathParam("noteId") UUID noteId) {
        String username = securityContext.getUserPrincipal().getName();
        boolean isAdmin = securityContext.isUserInRole("ADMIN");

        return noteService.findById(noteId)
                .filter(note -> isAdmin ||
                       (note.getCreatedBy() != null && note.getCreatedBy().getLogin().equals(username)))
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND)).build();
    }

    @DELETE
    @Path("/{noteId}")
    @RolesAllowed({"ADMIN", "USER"})
    public Response deleteNoteById(@PathParam("noteId") UUID noteId) {
        String username = securityContext.getUserPrincipal().getName();
        boolean isAdmin = securityContext.isUserInRole("ADMIN");

        return noteService.findById(noteId)
                .filter(note -> isAdmin ||
                       (note.getCreatedBy() != null && note.getCreatedBy().getLogin().equals(username)))
                .map(note -> {
                    noteService.delete(noteId);
                    return Response.noContent().build();
                })
                .orElse(Response.status(Response.Status.FORBIDDEN).build());
    }
}
