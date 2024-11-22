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

            // Enable the uuid-ossp extension for UUID generation
            String enableUuidExtension = "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";";
            stmt.executeUpdate(enableUuidExtension);

            // Create users table
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "id UUID DEFAULT uuid_generate_v4(), " +
                    "email VARCHAR(255) NOT NULL UNIQUE, " +
                    "password_hash VARCHAR(255) NOT NULL" +
                    ");";
            stmt.executeUpdate(sql);

            // Create passwords table with salt column
            String createPasswordsTable = "CREATE TABLE IF NOT EXISTS passwords (" +
                    "user_email VARCHAR(255) REFERENCES users(email) ON DELETE CASCADE, " +
                    "password_id UUID DEFAULT uuid_generate_v4(), " +
                    "description VARCHAR(255), " +
                    "encrypted_password TEXT NOT NULL, " +
                    "salt BYTEA NOT NULL, " +
                    "PRIMARY KEY (user_email, password_id)" +
                    ");";
            stmt.executeUpdate(createPasswordsTable);

            System.out.println("Database initialized.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}