import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Vault {

    private static final Path VAULT_DIRECTORY =
            Paths.get("vault").toAbsolutePath().normalize();

    private static final Path RECOVERY_DIRECTORY =
            Paths.get("recovered").toAbsolutePath().normalize();

    private final User user;
    private final Path userVaultPath;
    private final Path userRecoveryPath;

    public Vault(User user) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "L'utilisateur ne peut pas être null."
            );
        }

        this.user = user;

        validateUsername(user.getUsername());

        this.userVaultPath =
                createSafeUserPath(
                        VAULT_DIRECTORY,
                        user.getUsername()
                );

        this.userRecoveryPath =
                createSafeUserPath(
                        RECOVERY_DIRECTORY,
                        user.getUsername()
                );

        createVault();
        createRecoveryDirectory();
    }

    private void validateUsername(String username) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Nom d'utilisateur invalide."
            );
        }

        /*
         * On autorise uniquement :
         * lettres
         * chiffres
         * underscore
         * tiret
         *
         * Cela évite notamment :
         * /
         * \
         * ..
         * espaces
         * chemins absolus
         */
        if (!username.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException(
                    "Le nom d'utilisateur contient des caractères interdits."
            );
        }
    }

    private Path createSafeUserPath(
            Path baseDirectory,
            String username
    ) {

        Path normalizedBase =
                baseDirectory.toAbsolutePath().normalize();

        Path candidate =
                normalizedBase
                        .resolve(username)
                        .normalize();

        if (!candidate.startsWith(normalizedBase)) {
            throw new SecurityException(
                    "Tentative de sortie du répertoire autorisé."
            );
        }

        return candidate;
    }

    private void createVault() {

        try {

            Files.createDirectories(
                    userVaultPath
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Impossible de créer le coffre de l'utilisateur.",
                    e
            );
        }
    }

    private void createRecoveryDirectory() {

        try {

            Files.createDirectories(
                    userRecoveryPath
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Impossible de créer le dossier de récupération.",
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

    public Path getUserRecoveryPath() {
        return userRecoveryPath;
    }
}