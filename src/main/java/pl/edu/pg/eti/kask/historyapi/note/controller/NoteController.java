package pl.edu.pg.eti.kask.historyapi.note.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriBuilder;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.service.HistoricalFigureService;
import pl.edu.pg.eti.kask.historyapi.note.entity.Note;
import pl.edu.pg.eti.kask.historyapi.note.service.NoteService;
import pl.edu.pg.eti.kask.historyapi.user.service.UserService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/figures/{figureId}/notes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NoteController {

    @Inject
    private NoteService noteService;

    @Inject
    private HistoricalFigureService figureService;

    @Inject
    private UserService userService;

    @Context
    private SecurityContext securityContext;

    @GET
    @Path("/")
    @RolesAllowed({"ADMIN", "USER"})
    public Response getNotesForFigure(@PathParam("figureId") UUID figureId) {
        String username = securityContext.getUserPrincipal().getName();
        boolean isAdmin = securityContext.isUserInRole("ADMIN");

        List<Note> notes;
        if (isAdmin) {
            notes = noteService.findByFigureId(figureId);
        } else {
            notes = noteService.findByFigureIdAndOwner(figureId, username);
        }

        if(notes.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(notes).build();
    }

    @GET
    @Path("/{noteId}")
    @RolesAllowed({"ADMIN", "USER"})
    public Response getNoteById(@PathParam("figureId") UUID figureId,
                                @PathParam("noteId") UUID noteId) {
        String username = securityContext.getUserPrincipal().getName();
        boolean isAdmin = securityContext.isUserInRole("ADMIN");

        return noteService.findById(noteId)
                .filter(note -> note.getHistoricalFigure() != null &&
                               note.getHistoricalFigure().getId().equals(figureId))
                .filter(note -> isAdmin ||
                       (note.getCreatedBy() != null && note.getCreatedBy().getLogin().equals(username)))
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND)).build();
    }

    @POST
    @Path("/")
    @RolesAllowed({"ADMIN", "USER"})
    public Response createNoteForFigure(@PathParam("figureId") UUID figureId, Note note) {
        String username = securityContext.getUserPrincipal().getName();

        return figureService.findById(figureId)
                .flatMap(figure -> userService.findByLogin(username)
                    .map(user -> {
                        noteService.createNote(note, figure, user);
                        URI location = UriBuilder.fromPath("/figures/{figureId}/notes/{noteId}")
                                .build(figureId, note.getId());
                        return Response.created(location).build();
                    }))
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Path("/{noteId}")
    @RolesAllowed({"ADMIN", "USER"})
    public Response updateNote(@PathParam("figureId") UUID figureId,
                               @PathParam("noteId") UUID noteId,
                               Note note) {
        String username = securityContext.getUserPrincipal().getName();
        boolean isAdmin = securityContext.isUserInRole("ADMIN");

        return noteService.findById(noteId)
                .filter(existingNote -> isAdmin ||
                       (existingNote.getCreatedBy() != null &&
                        existingNote.getCreatedBy().getLogin().equals(username)))
                .flatMap(existingNote -> figureService.findById(figureId)
                    .map(figure -> {
                        note.setId(noteId);
                        note.setHistoricalFigure(figure);
                        note.setCreatedBy(existingNote.getCreatedBy());
                        noteService.save(note);
                        return Response.ok(note).build();
                    }))
                .orElse(Response.status(Response.Status.FORBIDDEN).build());
    }

    @DELETE
    @Path("/{noteId}")
    @RolesAllowed({"ADMIN", "USER"})
    public Response deleteHierarchicalNote(@PathParam("noteId") UUID noteId) {
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