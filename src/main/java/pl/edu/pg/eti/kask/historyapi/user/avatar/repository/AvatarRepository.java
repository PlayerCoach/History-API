package pl.edu.pg.eti.kask.historyapi.user.avatar.repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Optional;
import java.util.UUID;

public class AvatarRepository {
    private final Path storageDirectory;

    public AvatarRepository(String directoryPath) {
        this.storageDirectory = Paths.get(directoryPath);
        if(!Files.exists(storageDirectory)) {
            try {
                Files.createDirectories(storageDirectory);
            } catch (IOException e) {
                throw new RuntimeException("Could not create storage directory", e);
            }
        }
    }

    public void save(String filename, InputStream data) throws IOException {
        //Check if the file already exists, if so replace it
        if (Files.exists(storageDirectory.resolve(filename))) {
            Files.delete(storageDirectory.resolve(filename));
        }
        Files.copy(data, storageDirectory.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
    }

    public Optional<File> find(String filename) {
        File file = storageDirectory.resolve(filename).toFile();
        return file.exists() ? Optional.of(file) : Optional.empty();
    }

    public boolean delete(String filename) {
        File file = storageDirectory.resolve(filename).toFile();
        return file.exists() && file.delete();
    }

    public Optional<File> findAvatarFileByUuid(UUID userId) throws IOException {
        // This looks for any file that starts with the UUID string (e.g., "uuid.jpg", "uuid.png", etc.)
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(storageDirectory, userId.toString() + ".*")) {
            for (Path entry : stream) {
                // Return the first match found (should be one)
                return Optional.of(entry.toFile());
            }
        }
        // No match found
        return Optional.empty();
    }
}
