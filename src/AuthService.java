import java.util.ArrayList;
import java.util.List;

public class AuthService {

    private List<User> users;

    public AuthService() {
        users = new ArrayList<>();
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

        return true;
    }

    public boolean login(String username, String password) {
        User user = findUser(username);

        if (user == null) {
            return false;
        }

        return user.getPassword().equals(password);
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