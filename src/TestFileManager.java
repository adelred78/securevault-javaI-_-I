import java.nio.file.Path;
import java.util.List;

public class TestFileManager {

    public static void main(String[] args) {

        User user = new User("adel", "test123");
        Vault vault = new Vault(user);

        FileManager fileManager = new FileManager();

        Path sourceFile = Path.of("test.txt");

        List<String> files = fileManager.listFiles(
                vault.getUserVaultPath()
        );

        System.out.println("Fichiers actuels :");
        for (String file : files) {
            System.out.println("- " + file);
        }

        boolean added = fileManager.addFile(
                sourceFile,
                vault.getUserVaultPath()
        );

        System.out.println("Ajout : " + added);

        files = fileManager.listFiles(
                vault.getUserVaultPath()
        );

        System.out.println("Fichiers après ajout :");
        for (String file : files) {
            System.out.println("- " + file);
        }

        boolean deleted = fileManager.deleteFile(
                "test.txt",
                vault.getUserVaultPath()
        );

        System.out.println("Suppression : " + deleted);
    }
}