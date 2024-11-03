import org.springframework.security.crypto.bcrypt.BCrypt;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void register(String email, String password) throws Exception {
        if (userRepository.findByEmail(email) != null) {
            throw new Exception("Registration failed.");
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User(email, hashedPassword);
        userRepository.save(user);
    }

    public boolean login(String email, String password) {
        User user = userRepository.findByEmail(email);
        return user != null && BCrypt.checkpw(password, user.getPasswordHash());
    }
}
