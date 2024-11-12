import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.UUID;

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

    public void addPassword(String email, String description, String plainPassword) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("User not found.");
        }

        byte[] salt = EncryptionUtils.generateSalt();
        SecretKey secretKey = EncryptionUtils.deriveKeyFromPassword(user.getPasswordHash(), salt);
        String encryptedPassword = EncryptionUtils.encrypt(plainPassword, secretKey);

        userRepository.savePassword(email, description, encryptedPassword, salt);
    }

    public int findUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            return user.getId();
        }
        return -1;
    }
    public String decryptPassword(String email, String encryptedPassword, byte[] salt) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("User not found.");
        }

        SecretKey secretKey = EncryptionUtils.deriveKeyFromPassword(user.getPasswordHash(), salt);
        return EncryptionUtils.decrypt(encryptedPassword, secretKey);
    }

    public void deletePassword(UUID passwordId, String email) throws Exception {
        if (!userRepository.deletePasswordByIdAndEmail(passwordId, email)) {
            throw new Exception("Password entry not found.");
        }
    }


    public void modifyPassword(UUID passwordId, String newDescription, String newPlainPassword, User user) throws Exception {
        byte[] salt = EncryptionUtils.generateSalt();
        SecretKey secretKey = EncryptionUtils.deriveKeyFromPassword(user.getPasswordHash(), salt);
        String newEncryptedPassword = EncryptionUtils.encrypt(newPlainPassword, secretKey);

        if (!userRepository.updatePassword(passwordId, newDescription, newEncryptedPassword, salt)) {
            throw new Exception("Failed to update password entry.");
        }
    }

    public List<PasswordEntry> getPasswordsForUser(String email) {
        return userRepository.findPasswordsByEmail(email);
    }
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email) != null;
    }

}
