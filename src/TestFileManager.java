import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Path;
import java.util.List;

public class TestFileManager {

    public static void main(String[] args)
            throws Exception {

        CryptoService cryptoService =
                new CryptoService();

        String password = "test123";

        CryptoService.PasswordData passwordData =
                cryptoService.hashPassword(password);

        User user =
                new User(
                        "adel",
                        passwordData.getHash(),
                        passwordData.getSalt()
                );

        Vault vault =
                new Vault(user);

        FileManager fileManager =
                new FileManager();

        SecretKeySpec encryptionKey =
                cryptoService.deriveEncryptionKey(
                        password,
                        user.getPasswordSalt()
                );

        Path sourceFile =
                Path.of("test.txt");

        System.out.println(
                "Fichiers actuels :"
        );

        List<String> files =
                fileManager.listFiles(
                        vault.getUserVaultPath()
                );

        for (String file : files) {

            System.out.println(
                    "- " + file
            );
        }

        boolean added =
                fileManager.addEncryptedFile(
                        sourceFile,
                        vault.getUserVaultPath(),
                        encryptionKey
                );

        System.out.println(
                "Ajout chiffré : " + added
        );

        files =
                fileManager.listFiles(
                        vault.getUserVaultPath()
                );

        System.out.println(
                "Fichiers après ajout :"
        );

        for (String file : files) {

            System.out.println(
                    "- " + file
            );
        }
    }
}