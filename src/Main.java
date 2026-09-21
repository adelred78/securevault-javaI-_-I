import javax.crypto.spec.SecretKeySpec;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner =
                new Scanner(System.in);

        AuthService authService =
                new AuthService();

        AuditLogger auditLogger =
                new AuditLogger();

        auditLogger.log(
                "APPLICATION_STARTED",
                "SYSTEM",
                "Secure Vault started"
        );

        boolean running = true;

        while (running) {

            System.out.println();
            System.out.println(
                    "================================"
            );
            System.out.println(
                    "          SECURE VAULT"
            );
            System.out.println(
                    "================================"
            );
            System.out.println();

            System.out.println(
                    "1. Créer un compte"
            );

            System.out.println(
                    "2. Se connecter"
            );

            System.out.println(
                    "3. Quitter"
            );

            System.out.println();

            System.out.print(
                    "Votre choix : "
            );

            String choice =
                    scanner.nextLine();

            switch (choice) {

                case "1":

                    createAccount(
                            scanner,
                            authService,
                            auditLogger
                    );

                    break;

                case "2":

                    login(
                            scanner,
                            authService,
                            auditLogger
                    );

                    break;

                case "3":

                    running = false;

                    auditLogger.log(
                            "APPLICATION_STOPPED",
                            "SYSTEM",
                            "Secure Vault stopped"
                    );

                    System.out.println(
                            "Fermeture de Secure Vault..."
                    );

                    break;

                default:

                    System.out.println(
                            "Choix invalide."
                    );

                    auditLogger.log(
                            "INVALID_MENU_CHOICE",
                            "SYSTEM",
                            "choice=" + choice
                    );

                    break;
            }
        }

        scanner.close();
    }

    private static void createAccount(
            Scanner scanner,
            AuthService authService,
            AuditLogger auditLogger
    ) {

        System.out.println();
        System.out.println(
                "===== CRÉATION DU COMPTE ====="
        );

        System.out.print(
                "Nom d'utilisateur : "
        );

        String username =
                scanner.nextLine();

        if (username.isBlank()) {

            System.out.println(
                    "Le nom d'utilisateur ne peut pas être vide."
            );

            auditLogger.log(
                    "ACCOUNT_CREATION_FAILED",
                    "UNKNOWN",
                    "empty username"
            );

            return;
        }

        System.out.print(
                "Mot de passe : "
        );

        String password =
                scanner.nextLine();

        if (password.isBlank()) {

            System.out.println(
                    "Le mot de passe ne peut pas être vide."
            );

            auditLogger.log(
                    "ACCOUNT_CREATION_FAILED",
                    username,
                    "empty password"
            );

            return;
        }

        boolean created =
                authService.createUser(
                        username,
                        password
                );

        if (created) {

            System.out.println(
                    "Compte créé avec succès."
            );

            auditLogger.log(
                    "ACCOUNT_CREATED",
                    username,
                    "user account created"
            );

        } else {

            System.out.println(
                    "Impossible de créer le compte."
            );

            auditLogger.log(
                    "ACCOUNT_CREATION_FAILED",
                    username,
                    "username already exists or invalid"
            );
        }
    }

    private static void login(
            Scanner scanner,
            AuthService authService,
            AuditLogger auditLogger
    ) {

        System.out.println();
        System.out.println(
                "===== CONNEXION ====="
        );

        System.out.print(
                "Nom d'utilisateur : "
        );

        String username =
                scanner.nextLine();

        if (username.isBlank()) {

            System.out.println(
                    "Le nom d'utilisateur ne peut pas être vide."
            );

            return;
        }

        System.out.print(
                "Mot de passe : "
        );

        String password =
                scanner.nextLine();

        if (password.isBlank()) {

            System.out.println(
                    "Le mot de passe ne peut pas être vide."
            );

            return;
        }

        AuthService.LoginResult result =
                authService.login(
                        username,
                        password
                );

        if (result
                == AuthService.LoginResult.LOCKED) {

            System.out.println(
                    "Compte temporairement bloqué."
            );

            System.out.println(
                    "Veuillez patienter 30 secondes."
            );

            auditLogger.log(
                    "LOGIN_LOCKED",
                    username,
                    "account temporarily locked"
            );

            return;
        }

        if (result
                == AuthService.LoginResult.INVALID) {

            System.out.println(
                    "Identifiants incorrects."
            );

            auditLogger.log(
                    "LOGIN_FAILED",
                    username,
                    "invalid credentials"
            );

            return;
        }

        User user =
                authService.getUser(
                        username
                );

        if (user == null) {

            System.out.println(
                    "Erreur : utilisateur introuvable."
            );

            return;
        }

        System.out.println(
                "Connexion réussie."
        );

        System.out.println(
                "Bienvenue "
                        + user.getUsername()
                        + " !"
        );

        auditLogger.log(
                "LOGIN_SUCCESS",
                username,
                "authentication successful"
        );

        try {

            CryptoService cryptoService =
                    new CryptoService();

            SecretKeySpec encryptionKey =
                    cryptoService.deriveEncryptionKey(
                            password,
                            user.getPasswordSalt()
                    );

            Vault vault =
                    new Vault(user);

            vaultMenu(
                    scanner,
                    vault,
                    encryptionKey,
                    auditLogger
            );

        } catch (RuntimeException e) {

            System.out.println(
                    "Erreur : impossible d'ouvrir le coffre."
            );

            auditLogger.log(
                    "VAULT_OPEN_FAILED",
                    username,
                    "vault initialization failed"
            );
        }
    }

    private static void vaultMenu(
            Scanner scanner,
            Vault vault,
            SecretKeySpec encryptionKey,
            AuditLogger auditLogger
    ) {

        FileManager fileManager =
                new FileManager();

        boolean loggedIn = true;

        String username =
                vault.getUser()
                        .getUsername();

        while (loggedIn) {

            System.out.println();
            System.out.println(
                    "================================"
            );

            System.out.println(
                    "           MON COFFRE"
            );

            System.out.println(
                    "================================"
            );

            System.out.println();

            System.out.println(
                    "Utilisateur : "
                            + username
            );

            System.out.println(
                    "Coffre : "
                            + vault.getUserVaultPath()
            );

            System.out.println();

            System.out.println(
                    "1. Ajouter un fichier"
            );

            System.out.println(
                    "2. Lister mes fichiers"
            );

            System.out.println(
                    "3. Récupérer un fichier"
            );

            System.out.println(
                    "4. Supprimer un fichier"
            );

            System.out.println(
                    "5. Se déconnecter"
            );

            System.out.println();

            System.out.print(
                    "Votre choix : "
            );

            String choice =
                    scanner.nextLine();

            switch (choice) {

                case "1":

                    addFile(
                            scanner,
                            fileManager,
                            vault,
                            encryptionKey,
                            auditLogger
                    );

                    break;

                case "2":

                    listFiles(
                            fileManager,
                            vault
                    );

                    auditLogger.log(
                            "FILES_LISTED",
                            username,
                            "vault files listed"
                    );

                    break;

                case "3":

                    recoverFile(
                            scanner,
                            fileManager,
                            vault,
                            encryptionKey,
                            auditLogger
                    );

                    break;

                case "4":

                    deleteFile(
                            scanner,
                            fileManager,
                            vault,
                            auditLogger
                    );

                    break;

                case "5":

                    loggedIn = false;

                    System.out.println(
                            "Déconnexion réussie."
                    );

                    auditLogger.log(
                            "LOGOUT",
                            username,
                            "user logged out"
                    );

                    break;

                default:

                    System.out.println(
                            "Choix invalide."
                    );

                    auditLogger.log(
                            "INVALID_VAULT_MENU_CHOICE",
                            username,
                            "choice=" + choice
                    );

                    break;
            }
        }
    }

    private static void addFile(
            Scanner scanner,
            FileManager fileManager,
            Vault vault,
            SecretKeySpec encryptionKey,
            AuditLogger auditLogger
    ) {

        String username =
                vault.getUser()
                        .getUsername();

        System.out.println();
        System.out.println(
                "===== AJOUTER UN FICHIER ====="
        );

        System.out.print(
                "Chemin du fichier à ajouter : "
        );

        String filePath =
                scanner.nextLine();

        if (filePath.isBlank()) {

            System.out.println(
                    "Le chemin ne peut pas être vide."
            );

            return;
        }

        try {

            Path sourceFile =
                    Path.of(filePath);

            boolean added =
                    fileManager.addEncryptedFile(
                            sourceFile,
                            vault.getUserVaultPath(),
                            encryptionKey
                    );

            if (added) {

                String fileName =
                        sourceFile.getFileName()
                                .toString();

                System.out.println(
                        "Fichier chiffré et ajouté avec succès."
                );

                auditLogger.log(
                        "FILE_ADDED",
                        username,
                        "file=" + fileName + ".enc"
                );

            } else {

                System.out.println(
                        "Impossible d'ajouter le fichier."
                );

                auditLogger.log(
                        "FILE_ADD_FAILED",
                        username,
                        "file operation failed"
                );
            }

        } catch (InvalidPathException e) {

            System.out.println(
                    "Erreur : chemin de fichier invalide."
            );

            auditLogger.log(
                    "FILE_ADD_FAILED",
                    username,
                    "invalid path"
            );

        } catch (SecurityException e) {

            System.out.println(
                    "Erreur : accès au fichier refusé."
            );

            auditLogger.log(
                    "FILE_ADD_FAILED",
                    username,
                    "security exception"
            );
        }
    }

    private static void listFiles(
            FileManager fileManager,
            Vault vault
    ) {

        System.out.println();
        System.out.println(
                "===== MES FICHIERS ====="
        );

        List<String> files =
                fileManager.listFiles(
                        vault.getUserVaultPath()
                );

        if (files.isEmpty()) {

            System.out.println(
                    "Votre coffre est vide."
            );

            return;
        }

        for (int i = 0;
             i < files.size();
             i++) {

            System.out.println(
                    (i + 1)
                            + ". "
                            + files.get(i)
            );
        }
    }

    private static void recoverFile(
            Scanner scanner,
            FileManager fileManager,
            Vault vault,
            SecretKeySpec encryptionKey,
            AuditLogger auditLogger
    ) {

        String username =
                vault.getUser()
                        .getUsername();

        System.out.println();
        System.out.println(
                "===== RÉCUPÉRER UN FICHIER ====="
        );

        List<String> files =
                fileManager.listFiles(
                        vault.getUserVaultPath()
                );

        if (files.isEmpty()) {

            System.out.println(
                    "Votre coffre est vide."
            );

            return;
        }

        for (int i = 0;
             i < files.size();
             i++) {

            System.out.println(
                    (i + 1)
                            + ". "
                            + files.get(i)
            );
        }

        System.out.println();

        System.out.print(
                "Nom du fichier à récupérer : "
        );

        String fileName =
                scanner.nextLine();

        if (fileName.isBlank()) {

            System.out.println(
                    "Le nom du fichier ne peut pas être vide."
            );

            return;
        }

        boolean recovered =
                fileManager.decryptEncryptedFile(
                        fileName,
                        vault.getUserVaultPath(),
                        vault.getUserRecoveryPath(),
                        encryptionKey
                );

        if (recovered) {

            System.out.println(
                    "Fichier déchiffré avec succès."
            );

            System.out.println(
                    "Emplacement : "
                            + vault.getUserRecoveryPath()
            );

            auditLogger.log(
                    "FILE_RECOVERED",
                    username,
                    "file=" + fileName
            );

        } else {

            System.out.println(
                    "Impossible de récupérer le fichier."
            );

            auditLogger.log(
                    "FILE_RECOVERY_FAILED",
                    username,
                    "file=" + fileName
            );
        }
    }

    private static void deleteFile(
            Scanner scanner,
            FileManager fileManager,
            Vault vault,
            AuditLogger auditLogger
    ) {

        String username =
                vault.getUser()
                        .getUsername();

        System.out.println();
        System.out.println(
                "===== SUPPRIMER UN FICHIER ====="
        );

        List<String> files =
                fileManager.listFiles(
                        vault.getUserVaultPath()
                );

        if (files.isEmpty()) {

            System.out.println(
                    "Votre coffre est vide."
            );

            return;
        }

        for (int i = 0;
             i < files.size();
             i++) {

            System.out.println(
                    (i + 1)
                            + ". "
                            + files.get(i)
            );
        }

        System.out.println();

        System.out.print(
                "Nom du fichier à supprimer : "
        );

        String fileName =
                scanner.nextLine();

        if (fileName.isBlank()) {

            System.out.println(
                    "Le nom du fichier ne peut pas être vide."
            );

            return;
        }

        boolean deleted =
                fileManager.deleteFile(
                        fileName,
                        vault.getUserVaultPath()
                );

        if (deleted) {

            System.out.println(
                    "Fichier supprimé avec succès."
            );

            auditLogger.log(
                    "FILE_DELETED",
                    username,
                    "file=" + fileName
            );

        } else {

            System.out.println(
                    "Impossible de supprimer le fichier."
            );

            auditLogger.log(
                    "FILE_DELETE_FAILED",
                    username,
                    "file=" + fileName
            );
        }
    }
}