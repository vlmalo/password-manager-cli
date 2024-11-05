public class PasswordEntry {
    private int id;
    private String description;
    private String encryptedPassword;
    private byte[] salt;

    public PasswordEntry(int id, String description, String encryptedPassword, byte[] salt) {
        this.id = id;
        this.description = description;
        this.encryptedPassword = encryptedPassword;
        this.salt = salt;
    }

    public int getId() { return id; }

    public byte[] getSalt() { return salt; }
    public String getDescription() { return description; }
    public String getEncryptedPassword() { return encryptedPassword; }
}
