public class TestVault {

    public static void main(String[] args) {

        User user = new User("adel", "test123");

        Vault vault = new Vault(user);

        System.out.println("Utilisateur : "
                + vault.getUser().getUsername());

        System.out.println("Coffre : "
                + vault.getUserVaultPath());
    }
}