import java.util.UUID;

public class PasswordEntry {
    private UUID id;
    private String description;
    private String encryptedPassword;
    private byte[] salt;

    public PasswordEntry(UUID id, String description, String encryptedPassword, byte[] salt) {
        this.id = id;
        this.description = description;
        this.encryptedPassword = encryptedPassword;
        this.salt = salt;
    }

    public UUID getId() {
        return id;
    }

    public byte[] getSalt() { return salt; }
    public String getDescription() { return description; }
    public String getEncryptedPassword() { return encryptedPassword; }

    public static UUID generateNewId() {
        return UUID.randomUUID();
    }
}

