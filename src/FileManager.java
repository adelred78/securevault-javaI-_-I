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

    private void createDataDirectory() {
        try {
            Files.createDirectories(DATA_DIRECTORY);

            if (!Files.exists(USERS_FILE)) {
                Files.createFile(USERS_FILE);
            }
        } catch (IOException e) {
            System.out.println("Erreur lors de la préparation des données.");
        }
    }

    public void saveUser(User user) {
        String line = user.getUsername() + "|" + user.getPassword();

        try {
            Files.writeString(
                    USERS_FILE,
                    line + System.lineSeparator(),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.out.println("Erreur lors de la sauvegarde de l'utilisateur.");
        }
    }

    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(USERS_FILE);

            for (String line : lines) {
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split("\\|", 2);

                if (parts.length == 2) {
                    users.add(new User(parts[0], parts[1]));
                }
            }

        } catch (IOException e) {
            System.out.println("Erreur lors du chargement des utilisateurs.");
        }

        return users;
    }
}