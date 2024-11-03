import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
                String passwordHash = rs.getString("password_hash");
                return new User(email, passwordHash);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
