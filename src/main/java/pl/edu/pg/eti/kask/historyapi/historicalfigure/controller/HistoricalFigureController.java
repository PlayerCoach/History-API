package pl.edu.pg.eti.kask.historyapi.historicalfigure.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.entity.HistoricalFigure;
import pl.edu.pg.eti.kask.historyapi.historicalfigure.service.HistoricalFigureService;


import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/figures")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HistoricalFigureController {

    @Inject
    private HistoricalFigureService figureService;

    @GET
    @RolesAllowed({"ADMIN", "USER"})
    public List<HistoricalFigure> getAllFigures() {
        return figureService.findAll();
    }


    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    public Response getFigureById(@PathParam("id") UUID id) {
        return figureService.findById(id)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND)).build();
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response createFigure(HistoricalFigure figure) {
        figure.setId(UUID.randomUUID());
        figureService.save(figure);
        URI location = UriBuilder.fromPath("/figures/{id}").build(figure.getId());
        return Response.created(location).build(); // 201
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response updateFigure(@PathParam("id") UUID id, HistoricalFigure figure) {
        figure.setId(id);
        figureService.save(figure);
        return Response.ok(figure).build(); // 200
    }


    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response deleteFigure(@PathParam("id") UUID id) {
        //Service has cascade delete implemented
        figureService.delete(id);
        return Response.noContent().build(); // 204
    }
}