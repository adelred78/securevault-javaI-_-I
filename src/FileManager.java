import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    private static final Path DATA_DIRECTORY = Paths.get("data");
    private static final Path USERS_FILE = DATA_DIRECTORY.resolve("users.txt");

    public FileManager() {
        createDataDirectory();
    }

    // =========================
    // Gestion des utilisateurs
    // =========================

    private void createDataDirectory() {

        try {

            Files.createDirectories(DATA_DIRECTORY);

            if (!Files.exists(USERS_FILE)) {
                Files.createFile(USERS_FILE);
            }

        } catch (IOException e) {

            System.out.println(
                    "Erreur lors de la préparation des données."
            );
        }
    }

    public void saveUser(User user) {

        String line =
                user.getUsername()
                + "|"
                + user.getPasswordHash()
                + "|"
                + user.getPasswordSalt();

        try {

            Files.writeString(
                    USERS_FILE,
                    line + System.lineSeparator(),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND
            );

        } catch (IOException e) {

            System.out.println(
                    "Erreur lors de la sauvegarde de l'utilisateur."
            );
        }
    }

    public List<User> loadUsers() {

        List<User> users = new ArrayList<>();

        try {

            List<String> lines =
                    Files.readAllLines(USERS_FILE);

            for (String line : lines) {

                if (line.isBlank()) {
                    continue;
                }

                String[] parts =
                        line.split("\\|", 3);

                if (parts.length == 3) {

                    users.add(
                            new User(
                                    parts[0],
                                    parts[1],
                                    parts[2]
                            )
                    );
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "Erreur lors du chargement des utilisateurs."
            );
        }

        return users;
    }

    // =========================
    // Gestion des fichiers
    // =========================

    public boolean addFile(
            Path sourceFile,
            Path vaultPath
    ) {

        try {

            if (!Files.exists(sourceFile)) {
                return false;
            }

            if (!Files.isRegularFile(sourceFile)) {
                return false;
            }

            Path destination =
                    vaultPath.resolve(
                            sourceFile.getFileName()
                    );

            if (Files.exists(destination)) {
                return false;
            }

            Files.copy(
                    sourceFile,
                    destination
            );

            return true;

        } catch (IOException e) {

            return false;
        }
    }

    public List<String> listFiles(
            Path vaultPath
    ) {

        List<String> fileNames =
                new ArrayList<>();

        try {

            if (!Files.exists(vaultPath)) {
                return fileNames;
            }

            try (var stream = Files.list(vaultPath)) {

                stream
                        .filter(Files::isRegularFile)
                        .forEach(
                                path ->
                                        fileNames.add(
                                                path.getFileName()
                                                        .toString()
                                        )
                        );
            }

        } catch (IOException e) {

            System.out.println(
                    "Erreur lors de la lecture du coffre."
            );
        }

        return fileNames;
    }

    public boolean deleteFile(
            String fileName,
            Path vaultPath
    ) {

        try {

            Path fileToDelete =
                    vaultPath.resolve(fileName);

            if (!Files.exists(fileToDelete)) {
                return false;
            }

            if (!Files.isRegularFile(fileToDelete)) {
                return false;
            }

            Files.delete(fileToDelete);

            return true;

        } catch (IOException e) {

            return false;
        }
    }
}
