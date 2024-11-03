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
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try {
            userService.register(email, password);
            System.out.println("Registration successful!");
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void loginUser() {
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (userService.login(email, password)) {
            System.out.println("Login successful!");
            postLoginMenu();
        } else {
            System.out.println("Login failed. Check your email and password.");
        }
    }

    private void postLoginMenu() {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("Post Login Menu:");
            System.out.println("1. View Passwords");
            System.out.println("2. Logout");
            System.out.print("Please select an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("Displaying stored passwords (this is a placeholder).");
                    break;
                case 2:
                    loggedIn = false;
                    System.out.println("You have logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
