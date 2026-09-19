import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoService {

    private static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;

    private final SecureRandom secureRandom;

    public CryptoService() {
        secureRandom = new SecureRandom();
    }

    public PasswordData hashPassword(String password) {

        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);

        byte[] hash = deriveKey(password, salt);

        String encodedSalt =
                Base64.getEncoder().encodeToString(salt);

        String encodedHash =
                Base64.getEncoder().encodeToString(hash);

        return new PasswordData(encodedSalt, encodedHash);
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
                    deriveKey(password, salt);

            String actualHash =
                    Base64.getEncoder().encodeToString(hash);

            return actualHash.equals(expectedHash);

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] deriveKey(
            String password,
            byte[] salt
    ) {

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        try {

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(HASH_ALGORITHM);

            return factory.generateSecret(spec)
                    .getEncoded();

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Impossible de dériver le mot de passe.",
                    e
            );

        } finally {
            spec.clearPassword();
        }
    }

    public static class PasswordData {

        private final String salt;
        private final String hash;

        public PasswordData(String salt, String hash) {
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