import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    private static final Path DATA_DIRECTORY =
            Paths.get("data");

    private static final Path USERS_FILE =
            DATA_DIRECTORY.resolve("users.txt");

    public FileManager() {
        createDataDirectory();
    }

    // =========================
    // Gestion des utilisateurs
    // =========================

    private void createDataDirectory() {

        try {

            Files.createDirectories(
                    DATA_DIRECTORY
            );

            if (!Files.exists(USERS_FILE)) {
                Files.createFile(USERS_FILE);
            }

        } catch (IOException | SecurityException e) {

            System.out.println(
                    "Erreur : impossible de préparer les données."
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
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

        } catch (IOException | SecurityException e) {

            System.out.println(
                    "Erreur : impossible de sauvegarder l'utilisateur."
            );
        }
    }

    public List<User> loadUsers() {

        List<User> users =
                new ArrayList<>();

        try {

            if (!Files.exists(USERS_FILE)) {
                return users;
            }

            List<String> lines =
                    Files.readAllLines(
                            USERS_FILE
                    );

            for (String line : lines) {

                if (line.isBlank()) {
                    continue;
                }

                String[] parts =
                        line.split("\\|", 3);

                if (parts.length != 3) {

                    System.out.println(
                            "Une ligne utilisateur est invalide."
                    );

                    continue;
                }

                users.add(
                        new User(
                                parts[0],
                                parts[1],
                                parts[2]
                        )
                );
            }

        } catch (IOException | SecurityException e) {

            System.out.println(
                    "Erreur : impossible de charger les utilisateurs."
            );
        }

        return users;
    }

    // =========================
    // Protection des chemins
    // =========================

    private Path resolveInside(
            Path baseDirectory,
            String fileName
    ) {

        if (baseDirectory == null) {
            throw new SecurityException(
                    "Répertoire de base invalide."
            );
        }

        if (fileName == null || fileName.isBlank()) {
            throw new SecurityException(
                    "Nom de fichier invalide."
            );
        }

        Path normalizedBase =
                baseDirectory
                        .toAbsolutePath()
                        .normalize();

        Path candidate =
                normalizedBase
                        .resolve(fileName)
                        .normalize();

        if (!candidate.startsWith(normalizedBase)) {
            throw new SecurityException(
                    "Accès refusé : le chemin sort du répertoire autorisé."
            );
        }

        return candidate;
    }

    // =========================
    // Chiffrement
    // =========================

    public boolean addEncryptedFile(
            Path sourceFile,
            Path vaultPath,
            SecretKeySpec encryptionKey
    ) {

        try {

            if (sourceFile == null) {

                System.out.println(
                        "Erreur : fichier source invalide."
                );

                return false;
            }

            if (!Files.exists(sourceFile)) {

                System.out.println(
                        "Erreur : le fichier source n'existe pas."
                );

                return false;
            }

            if (!Files.isRegularFile(sourceFile)) {

                System.out.println(
                        "Erreur : le chemin indiqué n'est pas un fichier."
                );

                return false;
            }

            if (encryptionKey == null) {

                System.out.println(
                        "Erreur : clé de chiffrement indisponible."
                );

                return false;
            }

            /*
             * getFileName() récupère uniquement :
             *
             * test.txt
             *
             * et non :
             *
             * C:\Users\adel\test.txt
             */
            Path fileNamePath =
                    sourceFile.getFileName();

            if (fileNamePath == null) {

                System.out.println(
                        "Erreur : nom de fichier invalide."
                );

                return false;
            }

            String fileName =
                    fileNamePath.toString();

            if (fileName.isBlank()) {

                System.out.println(
                        "Erreur : nom de fichier invalide."
                );

                return false;
            }

            Path destination =
                    resolveInside(
                            vaultPath,
                            fileName + ".enc"
                    );

            if (Files.exists(destination)) {

                System.out.println(
                        "Erreur : ce fichier existe déjà dans le coffre."
                );

                return false;
            }

            CryptoService cryptoService =
                    new CryptoService();

            cryptoService.encryptFile(
                    sourceFile,
                    destination,
                    encryptionKey
            );

            return true;

        } catch (IOException | SecurityException e) {

            System.out.println(
                    "Erreur lors du chiffrement du fichier."
            );

            return false;
        }
    }

    // =========================
    // Listing
    // =========================

    public List<String> listFiles(
            Path vaultPath
    ) {

        List<String> fileNames =
                new ArrayList<>();

        try {

            if (vaultPath == null) {

                System.out.println(
                        "Erreur : chemin du coffre invalide."
                );

                return fileNames;
            }

            Path normalizedVault =
                    vaultPath
                            .toAbsolutePath()
                            .normalize();

            if (!Files.exists(normalizedVault)) {
                return fileNames;
            }

            if (!Files.isDirectory(normalizedVault)) {

                System.out.println(
                        "Erreur : le coffre n'est pas un dossier."
                );

                return fileNames;
            }

            try (var stream =
                         Files.list(normalizedVault)) {

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

        } catch (IOException | SecurityException e) {

            System.out.println(
                    "Erreur : impossible de lire le coffre."
            );
        }

        return fileNames;
    }

    // =========================
    // Déchiffrement
    // =========================

    public boolean decryptEncryptedFile(
            String encryptedFileName,
            Path vaultPath,
            Path recoveryPath,
            SecretKeySpec encryptionKey
    ) {

        try {

            if (encryptedFileName == null
                    || encryptedFileName.isBlank()) {

                System.out.println(
                        "Erreur : nom de fichier invalide."
                );

                return false;
            }

            if (!encryptedFileName.endsWith(".enc")) {

                System.out.println(
                        "Erreur : ce fichier n'est pas un fichier chiffré."
                );

                return false;
            }

            if (vaultPath == null
                    || recoveryPath == null) {

                System.out.println(
                        "Erreur : chemin de stockage invalide."
                );

                return false;
            }

            if (encryptionKey == null) {

                System.out.println(
                        "Erreur : clé de déchiffrement indisponible."
                );

                return false;
            }

            Path encryptedFile =
                    resolveInside(
                            vaultPath,
                            encryptedFileName
                    );

            if (!Files.exists(encryptedFile)) {

                System.out.println(
                        "Erreur : le fichier chiffré n'existe pas."
                );

                return false;
            }

            if (!Files.isRegularFile(encryptedFile)) {

                System.out.println(
                        "Erreur : l'élément sélectionné n'est pas un fichier."
                );

                return false;
            }

            String originalFileName =
                    encryptedFileName.substring(
                            0,
                            encryptedFileName.length() - 4
                    );

            if (originalFileName.isBlank()) {

                System.out.println(
                        "Erreur : nom de fichier invalide."
                );

                return false;
            }

            Path outputFile =
                    resolveInside(
                            recoveryPath,
                            originalFileName
                    );

            if (Files.exists(outputFile)) {

                System.out.println(
                        "Erreur : le fichier récupéré existe déjà."
                );

                return false;
            }

            Files.createDirectories(
                    recoveryPath
            );

            CryptoService cryptoService =
                    new CryptoService();

            cryptoService.decryptFile(
                    encryptedFile,
                    outputFile,
                    encryptionKey
            );

            return true;

        } catch (IOException | SecurityException e) {

            System.out.println(
                    "Erreur lors du déchiffrement du fichier."
            );

            return false;
        }
    }

    // =========================
    // Suppression
    // =========================

    public boolean deleteFile(
            String fileName,
            Path vaultPath
    ) {

        try {

            Path fileToDelete =
                    resolveInside(
                            vaultPath,
                            fileName
                    );

            if (!Files.exists(fileToDelete)) {

                System.out.println(
                        "Erreur : ce fichier n'existe pas."
                );

                return false;
            }

            if (!Files.isRegularFile(fileToDelete)) {

                System.out.println(
                        "Erreur : l'élément sélectionné n'est pas un fichier."
                );

                return false;
            }

            Files.delete(
                    fileToDelete
            );

            return true;

        } catch (IOException | SecurityException e) {

            System.out.println(
                    "Erreur lors de la suppression du fichier."
            );

            return false;
        }
    }
}
