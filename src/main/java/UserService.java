import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.UUID;

public class UserService {
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    public class ServiceException extends Exception {
        public ServiceException(String message) {
            super(message);
        }
    }

    private void validatePassword(String password) throws ServiceException {
        if (password.length() < 8) {
            throw new ServiceException("Password must be at least 8 characters long.");
        }
        if (!password.matches(".*[A-Za-z].*")) {
            throw new ServiceException("Password must contain at least one letter.");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new ServiceException("Password must contain at least one digit.");
        }
        if (!password.matches(".*[!@#$%^&*].*")) {
            throw new ServiceException("Password must contain at least one special character (!@#$%^&*).");
        }
    }



    private void validateEmail(String email) throws Exception {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!email.matches(emailRegex)) {
            throw new Exception("Invalid email format.");
        }
    }


    public void register(String email, String password) throws ServiceException {
        try {
            validateEmail(email);
            if (userRepository.findByEmail(email) != null) {
                throw new ServiceException("Registration failed.");
            }
            validatePassword(password);
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            User user = new User(email, hashedPassword);
            userRepository.save(user);
            logger.info("User registered successfully with email: {}", email);
        } catch (ServiceException e) {
            logger.error("Error during registration for email: {}", email, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during registration for email: {}", email, e);
            throw new ServiceException("An unexpected error occurred. Please try again later.");
        }
    }



    public boolean login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && BCrypt.checkpw(password, user.getPasswordHash())) {
            logger.info("Login successful for email: {}", email);
            return true;
        }
        logger.warn("Login failed for email: {}", email);
        return false;
    }

    public void addPassword(String email, String description, String plainPassword) throws Exception {
        try {
            if (plainPassword.length() < PasswordManagerCLI.MIN_PASSWORD_LENGTH || plainPassword.length() > PasswordManagerCLI.MAX_PASSWORD_LENGTH) {
                throw new ServiceException("Password must be between " + PasswordManagerCLI.MIN_PASSWORD_LENGTH + " and " + PasswordManagerCLI.MAX_PASSWORD_LENGTH + " characters long.");
            }
            if (!plainPassword.matches(PasswordManagerCLI.ALLOWED_PASSWORD_PATTERN)) {
                throw new ServiceException("Password contains invalid characters. Only letters, numbers, and the following special characters are allowed: !@#$%^&*");
            }

            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new ServiceException("User not found.");
            }

            byte[] salt = EncryptionUtils.generateSalt();
            SecretKey secretKey = EncryptionUtils.deriveKeyFromPassword(user.getPasswordHash(), salt);
            String encryptedPassword = EncryptionUtils.encrypt(plainPassword, secretKey);

            userRepository.savePassword(email, description, encryptedPassword, salt);
            logger.info("Password added for user email: {}", email);
        } catch (Exception e) {
            logger.error("Failed to add password for email: {}", email, e);
            throw e;
        }
    }


    public UUID findUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return user != null ? user.getId() : null;
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


    public void modifyPassword(UUID passwordId, String newDescription, String newPlainPassword, User user) throws ServiceException {
        try {
            if (newPlainPassword.length() < PasswordManagerCLI.MIN_PASSWORD_LENGTH || newPlainPassword.length() > PasswordManagerCLI.MAX_PASSWORD_LENGTH) {
                throw new ServiceException("Password must be between " + PasswordManagerCLI.MIN_PASSWORD_LENGTH + " and " + PasswordManagerCLI.MAX_PASSWORD_LENGTH + " characters long.");
            }
            if (!newPlainPassword.matches(PasswordManagerCLI.ALLOWED_PASSWORD_PATTERN)) {
                throw new ServiceException("Password contains invalid characters. Only letters, numbers, and the following special characters are allowed: !@#$%^&*");
            }


            byte[] salt = EncryptionUtils.generateSalt();
            SecretKey secretKey = EncryptionUtils.deriveKeyFromPassword(user.getPasswordHash(), salt);
            String newEncryptedPassword = EncryptionUtils.encrypt(newPlainPassword, secretKey);

            if (!userRepository.updatePassword(passwordId, newDescription, newEncryptedPassword, salt)) {
                throw new ServiceException("Failed to update password entry.");
            }
        } catch (Exception e) {
            logger.error("Failed to modify password for user: {}", user.getEmail(), e);
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