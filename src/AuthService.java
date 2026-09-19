import java.util.ArrayList;
import java.util.List;

public class AuthService {

    private final List<User> users;
    private final FileManager fileManager;
    private final CryptoService cryptoService;

    public AuthService() {
        fileManager = new FileManager();
        cryptoService = new CryptoService();
        users = new ArrayList<>(fileManager.loadUsers());
    }

    public boolean createUser(
            String username,
            String password
    ) {

        if (username == null || username.isBlank()) {
            return false;
        }

        if (password == null || password.isBlank()) {
            return false;
        }

        if (findUser(username) != null) {
            return false;
        }

        CryptoService.PasswordData passwordData =
                cryptoService.hashPassword(password);

        User user = new User(
                username,
                passwordData.getHash(),
                passwordData.getSalt()
        );

        users.add(user);
        fileManager.saveUser(user);

        return true;
    }

    public boolean login(
            String username,
            String password
    ) {

        User user = findUser(username);

        if (user == null) {
            return false;
        }

        return cryptoService.verifyPassword(
                password,
                user.getPasswordSalt(),
                user.getPasswordHash()
        );
    }

    public User getUser(String username) {
        return findUser(username);
    }

    private User findUser(String username) {

        for (User user : users) {

            if (user.getUsername().equals(username)) {
                return user;
            }
        }

        return null;
    }
}