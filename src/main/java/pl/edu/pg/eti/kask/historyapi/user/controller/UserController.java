package pl.edu.pg.eti.kask.historyapi.user.controller;

import pl.edu.pg.eti.kask.historyapi.user.avatar.AvatarService;
import pl.edu.pg.eti.kask.historyapi.user.entity.User;
import pl.edu.pg.eti.kask.historyapi.user.service.UserService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserController {

    private final UserService userService;
    private final AvatarService avatarService;
    public UserController(UserService userService, AvatarService avatarService) {
        this.userService = userService;
        this.avatarService = avatarService;
    }

    public List<User> getAllUsers() {
        return userService.getAll();
    }

    public Optional<User> getUser(UUID id) {
        return userService.get(id);
    }

    public void saveAvatar(UUID id, InputStream file) throws IOException {
        avatarService.save(id + ".jpg", file);
    }

    public File getAvatar(UUID id) {
        return avatarService.get(id + ".jpg");
    }

    public boolean deleteAvatar(UUID id) {
        return avatarService.delete(id + ".jpg");
    }
}
