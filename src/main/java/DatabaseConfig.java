import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static final String URL = "jdbc:postgresql://localhost:5432/userlist_db_cli";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Create users table
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "id SERIAL PRIMARY KEY, " +
                    "email VARCHAR(255) NOT NULL UNIQUE, " +
                    "password_hash VARCHAR(255) NOT NULL" +
                    ");";
            stmt.executeUpdate(sql);

            // Create passwords table with salt column
            String createPasswordsTable = "CREATE TABLE IF NOT EXISTS passwords (" +
                    "id SERIAL PRIMARY KEY, " +
                    "user_id INTEGER REFERENCES users(id), " +
                    "description VARCHAR(255), " +
                    "encrypted_password TEXT NOT NULL, " +
                    "salt BYTEA NOT NULL" +
                    ");";
            stmt.executeUpdate(createPasswordsTable);

            System.out.println("Database initialized.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
