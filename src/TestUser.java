public class TestUser {

    public static void main(String[] args) {

        AuthService authService = new AuthService();

        System.out.println("Création du compte : "
                + authService.createUser("adel", "test123"));

        System.out.println("Deuxième création : "
                + authService.createUser("adel", "anotherPassword"));

        System.out.println("Bonne connexion : "
                + authService.login("adel", "test123"));

        System.out.println("Mauvais mot de passe : "
                + authService.login("adel", "wrong"));

        System.out.println("Utilisateur inconnu : "
                + authService.login("sarah", "test123"));
    }
}