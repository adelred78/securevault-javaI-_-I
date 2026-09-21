import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestCrypto {

    public static void main(String[] args)
            throws Exception {

        CryptoService cryptoService =
                new CryptoService();

        String password = "test123";

        CryptoService.PasswordData passwordData =
                cryptoService.hashPassword(password);

        SecretKeySpec encryptionKey =
                cryptoService.deriveEncryptionKey(
                        password,
                        passwordData.getSalt()
                );

        Path input =
                Path.of("test.txt");

        Path encrypted =
                Path.of("test.txt.enc");

        Path decrypted =
                Path.of("test_decrypted.txt");

        cryptoService.encryptFile(
                input,
                encrypted,
                encryptionKey
        );

        System.out.println(
                "Chiffrement terminé."
        );

        cryptoService.decryptFile(
                encrypted,
                decrypted,
                encryptionKey
        );

        System.out.println(
                "Déchiffrement terminé."
        );

        String original =
                Files.readString(input);

        String restored =
                Files.readString(decrypted);

        System.out.println(
                "Contenu identique : "
                        + original.equals(restored)
        );
    }
}
