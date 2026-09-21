import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoService {

    private static final String PASSWORD_HASH_ALGORITHM =
            "PBKDF2WithHmacSHA256";

    private static final String ENCRYPTION_DERIVATION_ALGORITHM =
            "PBKDF2WithHmacSHA256";

    private static final String AES_ALGORITHM =
            "AES/GCM/NoPadding";

    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 12;

    private static final int PASSWORD_ITERATIONS = 120_000;
    private static final int ENCRYPTION_ITERATIONS = 120_000;

    private static final int KEY_LENGTH = 256;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecureRandom secureRandom;

    public CryptoService() {
        secureRandom = new SecureRandom();
    }

    // ==========================================
    // Protection des mots de passe
    // ==========================================

    public PasswordData hashPassword(String password) {

        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);

        byte[] hash = deriveKey(
                password,
                salt,
                PASSWORD_ITERATIONS
        );

        String encodedSalt =
                Base64.getEncoder().encodeToString(salt);

        String encodedHash =
                Base64.getEncoder().encodeToString(hash);

        return new PasswordData(
                encodedSalt,
                encodedHash
        );
    }

    public boolean verifyPassword(
            String password,
            String encodedSalt,
            String expectedHash
    ) {

        try {

            byte[] salt =
                    Base64.getDecoder().decode(encodedSalt);

            byte[] hash =
                    deriveKey(
                            password,
                            salt,
                            PASSWORD_ITERATIONS
                    );

            String actualHash =
                    Base64.getEncoder().encodeToString(hash);

            return actualHash.equals(expectedHash);

        } catch (IllegalArgumentException e) {

            return false;
        }
    }

    // ==========================================
    // Clé de chiffrement
    // ==========================================

    public SecretKeySpec deriveEncryptionKey(
            String password,
            String encodedSalt
    ) {

        byte[] salt =
                Base64.getDecoder().decode(encodedSalt);

        byte[] keyBytes =
                deriveKey(
                        password,
                        salt,
                        ENCRYPTION_ITERATIONS
                );

        return new SecretKeySpec(
                keyBytes,
                "AES"
        );
    }

    private byte[] deriveKey(
            String password,
            byte[] salt,
            int iterations
    ) {

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                iterations,
                KEY_LENGTH
        );

        try {

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            PASSWORD_HASH_ALGORITHM
                    );

            return factory.generateSecret(spec)
                    .getEncoded();

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Impossible de dériver la clé.",
                    e
            );

        } finally {

            spec.clearPassword();
        }
    }

    // ==========================================
    // Chiffrement d'un fichier
    // ==========================================

    public void encryptFile(
            Path inputFile,
            Path outputFile,
            SecretKeySpec key
    ) throws IOException {

        byte[] fileData =
                Files.readAllBytes(inputFile);

        byte[] iv =
                new byte[IV_LENGTH];

        secureRandom.nextBytes(iv);

        try {

            Cipher cipher =
                    Cipher.getInstance(AES_ALGORITHM);

            GCMParameterSpec gcmSpec =
                    new GCMParameterSpec(
                            GCM_TAG_LENGTH,
                            iv
                    );

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    key,
                    gcmSpec
            );

            byte[] encryptedData =
                    cipher.doFinal(fileData);

            byte[] result =
                    new byte[IV_LENGTH + encryptedData.length];

            System.arraycopy(
                    iv,
                    0,
                    result,
                    0,
                    IV_LENGTH
            );

            System.arraycopy(
                    encryptedData,
                    0,
                    result,
                    IV_LENGTH,
                    encryptedData.length
            );

            Files.write(
                    outputFile,
                    result
            );

        } catch (Exception e) {

            throw new IOException(
                    "Erreur lors du chiffrement du fichier.",
                    e
            );
        }
    }

    // ==========================================
    // Déchiffrement d'un fichier
    // ==========================================

    public void decryptFile(
            Path encryptedFile,
            Path outputFile,
            SecretKeySpec key
    ) throws IOException {

        byte[] encryptedFileData =
                Files.readAllBytes(encryptedFile);

        if (encryptedFileData.length <= IV_LENGTH) {
            throw new IOException(
                    "Fichier chiffré invalide."
            );
        }

        byte[] iv =
                new byte[IV_LENGTH];

        byte[] encryptedData =
                new byte[
                        encryptedFileData.length - IV_LENGTH
                ];

        System.arraycopy(
                encryptedFileData,
                0,
                iv,
                0,
                IV_LENGTH
        );

        System.arraycopy(
                encryptedFileData,
                IV_LENGTH,
                encryptedData,
                0,
                encryptedData.length
        );

        try {

            Cipher cipher =
                    Cipher.getInstance(AES_ALGORITHM);

            GCMParameterSpec gcmSpec =
                    new GCMParameterSpec(
                            GCM_TAG_LENGTH,
                            iv
                    );

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    key,
                    gcmSpec
            );

            byte[] decryptedData =
                    cipher.doFinal(encryptedData);

            Files.write(
                    outputFile,
                    decryptedData
            );

        } catch (Exception e) {

            throw new IOException(
                    "Impossible de déchiffrer le fichier.",
                    e
            );
        }
    }

    // ==========================================
    // Données de mot de passe
    // ==========================================

    public static class PasswordData {

        private final String salt;
        private final String hash;

        public PasswordData(
                String salt,
                String hash
        ) {

            this.salt = salt;
            this.hash = hash;
        }

        public String getSalt() {
            return salt;
        }

        public String getHash() {
            return hash;
        }
    }
}
