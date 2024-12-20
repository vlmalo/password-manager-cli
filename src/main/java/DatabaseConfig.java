import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static final String URL = "jdbc:postgresql://localhost:5432/userlist_db_cli";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    public static void initializeDatabase() {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Enable the uuid-ossp extension for UUID generation
            String enableUuidExtension = "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";";
            stmt.executeUpdate(enableUuidExtension);
            logger.info("UUID extension enabled.");

            // Create users table
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "id UUID DEFAULT uuid_generate_v4(), " +
                    "email VARCHAR(255) NOT NULL UNIQUE, " +
                    "password_hash VARCHAR(255) NOT NULL" +
                    ");";
            stmt.executeUpdate(sql);
            logger.info("Users table created or already exists.");

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
            logger.info("Passwords table created or already exists.");

            logger.info("Database initialized successfully.");
        } catch (SQLException e) {
            logger.error("Error initializing the database.", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
}