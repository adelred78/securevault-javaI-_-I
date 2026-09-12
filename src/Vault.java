import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Vault {

    private static final Path VAULT_DIRECTORY = Paths.get("vault");

    private final User user;
    private final Path userVaultPath;

    public Vault(User user) {
        this.user = user;
        this.userVaultPath = VAULT_DIRECTORY.resolve(user.getUsername());

        createVault();
    }

    private void createVault() {
        try {
            Files.createDirectories(userVaultPath);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Impossible de créer le coffre de l'utilisateur.",
                    e
            );
        }
    }

    public User getUser() {
        return user;
    }

    public Path getUserVaultPath() {
        return userVaultPath;
    }
}