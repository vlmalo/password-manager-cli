public class Password {
    private int id;
    private int userId;
    private String description;
    private String encryptedPassword;

    public Password(int id, int userId, String description, String encryptedPassword) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.encryptedPassword = encryptedPassword;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }
}