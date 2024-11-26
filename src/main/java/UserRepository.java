import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class UserRepository {
    private static final String URL = "jdbc:postgresql://localhost:5432/userlist_db_cli";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    public class RepositoryException extends RuntimeException {
        public RepositoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }


    public void save(User user) {
        String sql = "INSERT INTO users (email, password_hash) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswordHash());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error while saving user: {}", user.getEmail(), e);
            throw new RepositoryException("Failed to save user", e);
        }
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                UUID id = UUID.fromString(rs.getString("id"));
                String passwordHash = rs.getString("password_hash");
                logger.info("User found by email: {}", email);
                return new User(id, email, passwordHash);
            }
        } catch (SQLException e) {
            logger.error("Error while fetching user by email: {}", email, e);
            throw new RepositoryException("Failed to fetch user by email", e);
        }
        return null;
    }


    public void savePassword(String email, String description, String encryptedPassword, byte[] salt) {
        String sql = "INSERT INTO passwords (user_email, description, encrypted_password, salt) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, description);
            stmt.setString(3, encryptedPassword);
            stmt.setBytes(4, salt);
            stmt.executeUpdate();
            logger.info("Password saved successfully for user: {}", email);
        } catch (SQLException e) {
            logger.error("Error while saving password for user: {}", email, e);
            throw new RepositoryException("Failed to save password", e);
        }
    }

    public List<PasswordEntry> findPasswordsByEmail(String email) {
        List<PasswordEntry> passwords = new ArrayList<>();
        String sql = "SELECT * FROM passwords WHERE user_email = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("password_id"));
                String description = rs.getString("description");
                String encryptedPassword = rs.getString("encrypted_password");
                byte[] salt = rs.getBytes("salt");
                passwords.add(new PasswordEntry(id, description, encryptedPassword, salt));
            }
            logger.info("Fetched {} passwords for user: {}", passwords.size(), email);
        } catch (SQLException e) {
            logger.error("Error while fetching passwords for user: {}", email, e);
            throw new RepositoryException("Failed to fetch passwords", e);
        }
        return passwords;
    }

    public boolean deletePasswordByIdAndEmail(UUID passwordId, String email) {
        String sql = "DELETE FROM passwords WHERE password_id = ? AND user_email = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, passwordId);
            stmt.setString(2, email);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Error while deleting password for user: {} and password ID: {}", email, passwordId, e);
        }
        return false;
    }
    public boolean updatePassword(UUID passwordId, String newDescription, String newEncryptedPassword, byte[] newSalt) {
        String sql = "UPDATE passwords SET description = ?, encrypted_password = ?, salt = ? WHERE password_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newDescription);
            stmt.setString(2, newEncryptedPassword);
            stmt.setBytes(3, newSalt);
            stmt.setObject(4, passwordId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error while updating password for password ID: {}", passwordId, e);
        }
        return false;
    }

}