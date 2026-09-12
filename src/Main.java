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
            System.out.print("Entrez votre choix : ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1":
                    System.out.print("Nom d'utilisateur : ");
                    String username = scanner.nextLine();

                    System.out.print("Mot de passe : ");
                    String password = scanner.nextLine();

                    boolean created = authService.createUser(username, password);

                    if (created) {
                        System.out.println("Compte créé avec succès.");
                    } else {
                        System.out.println("Impossible de créer le compte.");
                    }
                    break;

                case "2":
                    System.out.print("Nom d'utilisateur : ");
                    String loginUsername = scanner.nextLine();

                    System.out.print("Mot de passe : ");
                    String loginPassword = scanner.nextLine();

                    boolean loggedIn = authService.login(
                            loginUsername,
                            loginPassword
                    );

                    if (loggedIn) {
                        System.out.println("Connexion réussie.");
                        System.out.println("Bienvenue " + loginUsername + " !");
                    } else {
                        System.out.println("Identifiants incorrects.");
                    }
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
}