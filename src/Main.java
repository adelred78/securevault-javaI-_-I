import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        AuthService authService = new AuthService();

        boolean running = true;

        while (running) {

            System.out.println();
            System.out.println("=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+");
            System.out.println("          SECURE VAULT");
            System.out.println("=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+");
            System.out.println();
            System.out.println("1. Créer un compte");
            System.out.println("2. Se connecter");
            System.out.println("3. Quitter");
            System.out.println();
            System.out.print("Votre choix : ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1":
                    createAccount(scanner, authService);
                    break;

                case "2":
                    login(scanner, authService);
                    break;

                case "3":
                    running = false;
                    System.out.println("Fermeture de Secure Vault...");
                    break;

                default:
                    System.out.println("Choix invalide.");
                    break;
            }
        }

        scanner.close();
    }

    private static void createAccount(
            Scanner scanner,
            AuthService authService
    ) {

        System.out.println();
        System.out.println("===== CRÉATION DU COMPTE =====");

        System.out.print("Nom d'utilisateur : ");
        String username = scanner.nextLine();

        System.out.print("Mot de passe : ");
        String password = scanner.nextLine();

        boolean created = authService.createUser(
                username,
                password
        );

        if (created) {
            System.out.println("Compte créé avec succès.");
        } else {
            System.out.println(
                    "Impossible de créer le compte."
            );
        }
    }

    private static void login(
            Scanner scanner,
            AuthService authService
    ) {

        System.out.println();
        System.out.println("===== CONNEXION =====");

        System.out.print("Nom d'utilisateur : ");
        String username = scanner.nextLine();

        System.out.print("Mot de passe : ");
        String password = scanner.nextLine();

        boolean loggedIn = authService.login(
                username,
                password
        );

        if (!loggedIn) {
            System.out.println("Identifiants incorrects.");
            return;
        }

        User user = authService.getUser(username);

        System.out.println("Connexion réussie.");
        System.out.println("Bienvenue " + user.getUsername() + " !");

        Vault vault = new Vault(user);

        vaultMenu(scanner, vault);
    }

    private static void vaultMenu(
            Scanner scanner,
            Vault vault
    ) {

        FileManager fileManager = new FileManager();

        boolean loggedIn = true;

        while (loggedIn) {

            System.out.println();
            System.out.println("================================");
            System.out.println("         MON COFFRE");
            System.out.println("================================");
            System.out.println();
            System.out.println(
                    "Utilisateur : "
                            + vault.getUser().getUsername()
            );
            System.out.println(
                    "Coffre : "
                            + vault.getUserVaultPath()
            );
            System.out.println();
            System.out.println("1. Ajouter un fichier");
            System.out.println("2. Lister mes fichiers");
            System.out.println("3. Supprimer un fichier");
            System.out.println("4. Se déconnecter");
            System.out.println();
            System.out.print("Votre choix : ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1":
                    addFile(scanner, fileManager, vault);
                    break;

                case "2":
                    listFiles(fileManager, vault);
                    break;

                case "3":
                    deleteFile(scanner, fileManager, vault);
                    break;

                case "4":
                    loggedIn = false;
                    System.out.println("Déconnexion réussie.");
                    break;

                default:
                    System.out.println("Choix invalide.");
                    break;
            }
        }
    }

    private static void addFile(
            Scanner scanner,
            FileManager fileManager,
            Vault vault
    ) {

        System.out.println();
        System.out.println("===== AJOUTER UN FICHIER =====");

        System.out.print(
                "Chemin du fichier à ajouter : "
        );

        String filePath = scanner.nextLine();

        Path sourceFile = Path.of(filePath);

        boolean added = fileManager.addFile(
                sourceFile,
                vault.getUserVaultPath()
        );

        if (added) {
            System.out.println(
                    "Fichier ajouté avec succès."
            );
        } else {
            System.out.println(
                    "Impossible d'ajouter le fichier."
            );
        }
    }

    private static void listFiles(
            FileManager fileManager,
            Vault vault
    ) {

        System.out.println();
        System.out.println("===== MES FICHIERS =====");

        List<String> files = fileManager.listFiles(
                vault.getUserVaultPath()
        );

        if (files.isEmpty()) {
            System.out.println(
                    "Votre coffre est vide."
            );
            return;
        }

        for (int i = 0; i < files.size(); i++) {
            System.out.println(
                    (i + 1) + ". " + files.get(i)
            );
        }
    }

    private static void deleteFile(
            Scanner scanner,
            FileManager fileManager,
            Vault vault
    ) {

        System.out.println();
        System.out.println("===== SUPPRIMER UN FICHIER =====");

        List<String> files = fileManager.listFiles(
                vault.getUserVaultPath()
        );

        if (files.isEmpty()) {
            System.out.println(
                    "Votre coffre est vide."
            );
            return;
        }

        for (int i = 0; i < files.size(); i++) {
            System.out.println(
                    (i + 1) + ". " + files.get(i)
            );
        }

        System.out.print(
                "Nom du fichier à supprimer : "
        );

        String fileName = scanner.nextLine();

        boolean deleted = fileManager.deleteFile(
                fileName,
                vault.getUserVaultPath()
        );

        if (deleted) {
            System.out.println(
                    "Fichier supprimé avec succès."
            );
        } else {
            System.out.println(
                    "Impossible de supprimer le fichier."
            );
        }
    }
}