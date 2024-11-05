import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.crypto.SecretKey;
import java.util.List;

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

    public void addPassword(int userId, String description, String plainPassword) throws Exception {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new Exception("User not found.");
        }

        byte[] salt = EncryptionUtils.generateSalt();
        SecretKey secretKey = EncryptionUtils.deriveKeyFromPassword(user.getPasswordHash(), salt);
        String encryptedPassword = EncryptionUtils.encrypt(plainPassword, secretKey);

        userRepository.savePassword(userId, description, encryptedPassword, salt);
    }
    public int findUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            return user.getId();
        }
        return -1;
    }
    public String decryptPassword(int userId, String encryptedPassword, byte[] salt) throws Exception {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new Exception("User not found.");
        }

        SecretKey secretKey = EncryptionUtils.deriveKeyFromPassword(user.getPasswordHash(), salt);
        return EncryptionUtils.decrypt(encryptedPassword, secretKey);
    }


    public List<PasswordEntry> getPasswordsForUser(int userId) {
        return userRepository.findPasswordsByUserId(userId);
    }

}
