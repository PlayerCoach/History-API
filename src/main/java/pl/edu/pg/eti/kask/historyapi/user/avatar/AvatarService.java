package pl.edu.pg.eti.kask.historyapi.user.avatar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class AvatarService {
    private final Path baseDir;

    public AvatarService(String directoryPath) {
        this.baseDir = Paths.get(directoryPath);
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create avatar directory: " + directoryPath, e);
        }
    }

    public void save(String filename, InputStream input) throws IOException {
        Files.copy(input, baseDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
    }

    public File get(String filename) {
        File file = baseDir.resolve(filename).toFile();
        return file.exists() ? file : null;
    }

    public boolean delete(String filename) {
        File file = baseDir.resolve(filename).toFile();
        return file.exists() && file.delete();
    }
}
