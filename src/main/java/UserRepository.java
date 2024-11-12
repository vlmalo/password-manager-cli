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

    public void save(User user) {
        String sql = "INSERT INTO users (email, password_hash) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswordHash());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                String passwordHash = rs.getString("password_hash");
                return new User(id, email, passwordHash);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String email = rs.getString("email");
                String passwordHash = rs.getString("password_hash");
                return new User(id, email, passwordHash);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        } catch (SQLException e) {
            e.printStackTrace();
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
                // Change from int to UUID
                String idString = rs.getString("password_id");  // Retrieve as string
                UUID id = UUID.fromString(idString);            // Convert to UUID
                String description = rs.getString("description");
                String encryptedPassword = rs.getString("encrypted_password");
                byte[] salt = rs.getBytes("salt");
                passwords.add(new PasswordEntry(id, description, encryptedPassword, salt));
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return false;
    }
    public boolean updatePassword(UUID passwordId, String newDescription, String newEncryptedPassword, byte[] newSalt) {
        String sql = "UPDATE passwords SET description = ?, encrypted_password = ?, salt = ? WHERE password_2id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newDescription);
            stmt.setString(2, newEncryptedPassword);
            stmt.setBytes(3, newSalt);
            stmt.setObject(4, passwordId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
