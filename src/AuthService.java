import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthService {

    private static final int MAX_LOGIN_ATTEMPTS = 3;

    private static final long LOCKOUT_DURATION_MS =
            30_000;

    private final List<User> users;
    private final FileManager fileManager;
    private final CryptoService cryptoService;

    private final Map<String, Integer> failedAttempts;
    private final Map<String, Long> lockoutTimes;

    public AuthService() {

        fileManager =
                new FileManager();

        cryptoService =
                new CryptoService();

        users =
                new ArrayList<>(
                        fileManager.loadUsers()
                );

        failedAttempts =
                new HashMap<>();

        lockoutTimes =
                new HashMap<>();
    }

    // =========================
    // Création de compte
    // =========================

    public boolean createUser(
            String username,
            String password
    ) {

        if (!isValidUsername(username)) {
            return false;
        }

        if (password == null
                || password.isBlank()) {

            return false;
        }

        if (findUser(username) != null) {

            return false;
        }

        CryptoService.PasswordData passwordData =
                cryptoService.hashPassword(
                        password
                );

        User user =
                new User(
                        username,
                        passwordData.getHash(),
                        passwordData.getSalt()
                );

        users.add(user);

        fileManager.saveUser(user);

        return true;
    }

    // =========================
    // Validation username
    // =========================

    private boolean isValidUsername(
            String username
    ) {

        if (username == null
                || username.isBlank()) {

            return false;
        }

        return username.matches(
                "[a-zA-Z0-9_-]+"
        );
    }

    // =========================
    // Connexion
    // =========================

    public LoginResult login(
            String username,
            String password
    ) {

        if (!isValidUsername(username)) {

            return LoginResult.INVALID;
        }

        if (password == null
                || password.isBlank()) {

            return LoginResult.INVALID;
        }

        User user =
                findUser(username);

        if (user == null) {

            return LoginResult.INVALID;
        }

        if (isLocked(username)) {

            return LoginResult.LOCKED;
        }

        boolean valid =
                cryptoService.verifyPassword(
                        password,
                        user.getPasswordSalt(),
                        user.getPasswordHash()
                );

        if (valid) {

            failedAttempts.remove(
                    username
            );

            lockoutTimes.remove(
                    username
            );

            return LoginResult.SUCCESS;
        }

        int attempts =
                failedAttempts.getOrDefault(
                        username,
                        0
                );

        attempts++;

        if (attempts >= MAX_LOGIN_ATTEMPTS) {

            failedAttempts.remove(
                    username
            );

            lockoutTimes.put(
                    username,
                    System.currentTimeMillis()
            );

            return LoginResult.LOCKED;
        }

        failedAttempts.put(
                username,
                attempts
        );

        return LoginResult.INVALID;
    }

    // =========================
    // Vérification verrouillage
    // =========================

    private boolean isLocked(
            String username
    ) {

        Long lockTime =
                lockoutTimes.get(
                        username
                );

        if (lockTime == null) {
            return false;
        }

        long elapsedTime =
                System.currentTimeMillis()
                        - lockTime;

        if (elapsedTime
                >= LOCKOUT_DURATION_MS) {

            lockoutTimes.remove(
                    username
            );

            failedAttempts.remove(
                    username
            );

            return false;
        }

        return true;
    }

    // =========================
    // Récupération utilisateur
    // =========================

    public User getUser(
            String username
    ) {

        return findUser(
                username
        );
    }

    // =========================
    // Recherche utilisateur
    // =========================

    private User findUser(
            String username
    ) {

        for (User user : users) {

            if (user.getUsername()
                    .equals(username)) {

                return user;
            }
        }

        return null;
    }

    // =========================
    // Résultat de connexion
    // =========================

    public enum LoginResult {

        SUCCESS,
        INVALID,
        LOCKED
    }
}