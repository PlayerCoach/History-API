package pl.edu.pg.eti.kask.historyapi.user.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.edu.pg.eti.kask.historyapi.user.avatar.service.AvatarService;
import pl.edu.pg.eti.kask.historyapi.user.dto.UserRegistrationDto;
import pl.edu.pg.eti.kask.historyapi.user.entity.Role;
import pl.edu.pg.eti.kask.historyapi.user.entity.User;
import pl.edu.pg.eti.kask.historyapi.user.service.UserService;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {

    @Inject
    private UserService userService;

    @Inject
    private AvatarService avatarService;

    @POST
    @PermitAll // każdy może się zarejestrować
    public Response registerUser(UserRegistrationDto dto) {
        // Check if user with this login already exists
        if (userService.findByLogin(dto.getLogin()).isPresent()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("User with login " + dto.getLogin() + " already exists")
                    .build();
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setLogin(dto.getLogin());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // Plain password - will be hashed in UserService
        user.setRole(Role.USER); // nowi użytkownicy domyślnie mają rolę USER

        userService.save(user);

        URI location = UriBuilder.fromPath("/users/{userId}")
                .build(user.getId());
        return Response.created(location).build();
    }

    @GET
    @RolesAllowed("ADMIN") // tylko admin może pobierać listę użytkowników
    public Response getAllUsers() {
        return Response.ok(userService.findAll()).build();
    }

    @GET
    @Path("/{userId}")
    @RolesAllowed("ADMIN") // tylko admin może pobierać pojedynczych użytkowników
    public Response getUserById(@PathParam("userId") UUID userId) {
        return userService.findById(userId)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @DELETE
    @Path("/{userId}")
    @RolesAllowed("ADMIN") // tylko admin może usuwać użytkowników
    public Response deleteUser(@PathParam("userId") UUID userId) {
        userService.delete(userId);
        return Response.noContent().build();
    }

    // Avatar endpoints
    @GET
    @Path("/{userId}/avatar")
    @Produces({"image/png", "image/jpeg"})
    @RolesAllowed({"ADMIN", "USER"}) // zalogowani użytkownicy mogą pobierać avatary
    public Response getUserAvatar(@PathParam("userId") UUID userId) throws IOException {
        return avatarService.find(userId)
                .map(file -> {
                    try {
                        InputStream is = new FileInputStream(file);
                        String mimeType = file.getName().endsWith(".png") ? "image/png" : "image/jpeg";
                        return Response.ok(is, mimeType).build();
                    } catch (Exception e) {
                        return Response.serverError().entity("Failed to read avatar file").build();
                    }
                })
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Avatar not found\"}").build());
    }

    @DELETE
    @Path("/{userId}/avatar")
    @RolesAllowed({"ADMIN", "USER"}) // zalogowani użytkownicy mogą usuwać avatary
    public Response deleteUserAvatar(@PathParam("userId") UUID userId) throws IOException {
        if (avatarService.delete(userId)) {
            return Response.ok("{\"message\": \"Avatar deleted\"}").build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Avatar not found\"}").build();
    }

    @POST
    @Path("/{userId}/avatar")
    @Consumes({"image/png", "image/jpeg", "image/jpg"})
    @RolesAllowed({"ADMIN", "USER"}) // zalogowani użytkownicy mogą uploadować avatary
    public Response uploadUserAvatar(@PathParam("userId") UUID userId,
                                     InputStream fileInputStream,
                                     @HeaderParam("Content-Type") String contentType) {
        // Wykryj rozszerzenie pliku z Content-Type
        String extension;
        if (contentType != null && (contentType.contains("jpeg") || contentType.contains("jpg"))) {
            extension = ".jpg";
        } else if (contentType != null && contentType.contains("png")) {
            extension = ".png";
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Unsupported file type. Only JPG and PNG allowed. Content-Type: " + contentType + "\"}").build();
        }

        try {
            avatarService.save(userId, extension, fileInputStream);
            return Response.status(Response.Status.CREATED)
                    .entity("{\"message\": \"Avatar uploaded successfully\"}").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\": \"Failed to upload avatar: " + e.getMessage() + "\"}").build();
        }
    }
}

