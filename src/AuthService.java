import java.util.ArrayList;
import java.util.List;

public class AuthService {

    private List<User> users;
    private FileManager fileManager;

    public AuthService() {
        fileManager = new FileManager();
        users = new ArrayList<>(fileManager.loadUsers());
    }

    public boolean createUser(String username, String password) {
        if (username == null || username.isBlank()) {
            return false;
        }

        if (password == null || password.isBlank()) {
            return false;
        }

        if (findUser(username) != null) {
            return false;
        }

        User user = new User(username, password);
        users.add(user);
        fileManager.saveUser(user);

        return true;
    }

    public boolean login(String username, String password) {
        User user = findUser(username);

        if (user == null) {
            return false;
        }

        return user.getPassword().equals(password);
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