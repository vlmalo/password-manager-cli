import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public void savePassword(int userId, String description, String encryptedPassword, byte[] salt) {
        String sql = "INSERT INTO passwords (user_id, description, encrypted_password, salt) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, description);
            stmt.setString(3, encryptedPassword);
            stmt.setBytes(4, salt);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public List<PasswordEntry> findPasswordsByUserId(int userId) {
        List<PasswordEntry> passwords = new ArrayList<>();
        String sql = "SELECT * FROM passwords WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
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

}
