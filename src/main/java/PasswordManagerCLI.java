import java.util.List;
import java.util.Scanner;

public class PasswordManagerCLI {
    private static final Scanner scanner = new Scanner(System.in);
    private final UserService userService;

    public PasswordManagerCLI(UserService userService) {
        this.userService = userService;
    }

    public static void main(String[] args) {
        DatabaseConfig.initializeDatabase();

        UserRepository userRepository = new UserRepository();
        UserService userService = new UserService(userRepository);
        PasswordManagerCLI cli = new PasswordManagerCLI(userService);
        cli.start();
    }

    private void start() {
        boolean running = true;

        while (running) {
            System.out.println("Welcome to the Password Manager!");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Please select an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    registerUser();
                    break;
                case 2:
                    loginUser();
                    break;
                case 3:
                    running = false;
                    System.out.println("Exiting the application. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void registerUser() {
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        try {
            userService.register(email, password);
            System.out.println("Registration successful!");
        } catch (Exception e) {
            System.out.println("Status: " + e.getMessage());
        }
    }

    private void loginUser() {
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        if (userService.login(email, password)) {
            System.out.println("Login successful!");
            postLoginMenu(email);
        } else {
            System.out.println("Login failed. Check your email and password.");
        }
    }

    private void postLoginMenu(String email) {
        boolean loggedIn = true;
        int userId = userService.findUserIdByEmail(email);

        System.out.println("Logged in user ID: " + userId);
        while (loggedIn) {
            System.out.println("Post Login Menu:");
            System.out.println("1. Add Password");
            System.out.println("2. View Passwords");
            System.out.println("3. Logout");
            System.out.print("Please select an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    addPassword(userId);
                    break;
                case 2:
                    viewPasswords(userId);
                    break;
                case 3:
                    loggedIn = false;
                    System.out.println("You have logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void viewPasswords(int userId) {
        List<PasswordEntry> passwords = userService.getPasswordsForUser(userId);
        if (passwords.isEmpty()) {
            System.out.println("No passwords found for this user.");
        } else {
            System.out.println("------");

            System.out.println("Stored passwords:");
            for (PasswordEntry passwordEntry : passwords) {
                try {
                    String decryptedPassword = userService.decryptPassword(userId, passwordEntry.getEncryptedPassword(), passwordEntry.getSalt());
                    System.out.println("Description: " + passwordEntry.getDescription());
                    System.out.println("Decrypted Password: " + decryptedPassword);
                } catch (Exception e) {
                    System.out.println("Error decrypting password: " + e.getMessage());
                }
                System.out.println("------");
            }
        }
    }

    private void addPassword(int userId) {
        System.out.print("Enter a description for the password: ");
        String description = scanner.nextLine().trim();
        System.out.print("Enter the password: ");
        String password = scanner.nextLine().trim();

        try {
            userService.addPassword(userId, description, password);
            System.out.println("Password added successfully!");
        } catch (Exception e) {
            System.out.println("Error adding password: " + e.getMessage());
        }
    }
}
