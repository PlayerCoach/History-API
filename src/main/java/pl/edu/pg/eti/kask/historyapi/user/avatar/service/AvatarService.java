package pl.edu.pg.eti.kask.historyapi.user.avatar.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import pl.edu.pg.eti.kask.historyapi.user.avatar.repository.AvatarRepository;
import pl.edu.pg.eti.kask.historyapi.user.repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;
@ApplicationScoped
public class AvatarService {
    private AvatarRepository avatarRepository;
    private  UserRepository userRepository;

    public AvatarService() {}
    @Inject
    public AvatarService(AvatarRepository avatarRepository, UserRepository userRepository) {
        this.avatarRepository = avatarRepository;
        this.userRepository = userRepository;
    }

    public void save(UUID userId, String extension, InputStream data) throws IOException {
        // Business Rule: Check if the user actually exists.
        if (userRepository.findById(userId).isEmpty()) {
            throw new IllegalArgumentException("Cannot save avatar. User with ID " + userId + " not found.");
        }

        // Business Rule: A user can only have one avatar. Delete the old one first.
        delete(userId);

        String filename = userId.toString() + extension;
        avatarRepository.save(filename, data);
    }


    public Optional<File> find(UUID userId) throws IOException {
        // Since we don't know the extension, we ask the repository to search for the file.
        return avatarRepository.findAvatarFileByUuid(userId);
    }

    public boolean delete(UUID userId) throws IOException {
        Optional<File> existingAvatar = avatarRepository.findAvatarFileByUuid(userId);

        // We get the full filename from the found file and ask the repository to delete it.
        return existingAvatar.filter(file -> avatarRepository.delete(file.getName())).isPresent();

    }
}
