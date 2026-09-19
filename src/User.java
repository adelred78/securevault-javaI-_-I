public class User {

    private final String username;
    private final String passwordHash;
    private final String passwordSalt;

    public User(
            String username,
            String passwordHash,
            String passwordSalt
    ) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }
}