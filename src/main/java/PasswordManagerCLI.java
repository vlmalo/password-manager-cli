
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.Console;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class PasswordManagerCLI {
    private static final Logger logger = LoggerFactory.getLogger(PasswordManagerCLI.class);
    private static final Scanner scanner = new Scanner(System.in);
    private final UserService userService;
    private final Map<String, Integer> failedAttempts = new HashMap<>();
    private final Map<String, Instant> blockedUsers = new HashMap<>();

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_MS = 300_000; // ms, 5 minutes

    // Patterns and length constraints for validations
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    public static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_EMAIL_LENGTH = 60;
    public static final int MAX_PASSWORD_LENGTH = 30;
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    public static final String ALLOWED_PASSWORD_PATTERN = "^[A-Za-z0-9!@#$%^&*]+$";
    private static final Pattern ALLOWED_DESCRIPTION_PATTERN = Pattern.compile("^[A-Za-z0-9 .,!?_-]*$");

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
            System.out.println("\n=== Welcome to the Password Manager ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Please select an option (1-3): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());

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
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private void registerUser() {
        System.out.println("\n=== User Registration ===");
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim().toLowerCase();

        if (!InputValidator.isValidInput(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            System.out.println("Invalid email format. Please try again.");
            logger.warn("Invalid email format: {}", email);
            return;
        }
        if (email.length() > MAX_EMAIL_LENGTH) {
            System.out.println("Email is too long. Maximum length is " + MAX_EMAIL_LENGTH + " characters.");
            return;
        }

        String password = maskPasswordInput();

        if (password.length() < MIN_PASSWORD_LENGTH) {
            System.out.println("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
            return;
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            System.out.println("Password is too long. Maximum length is " + MAX_PASSWORD_LENGTH + " characters.");
            return;
        }


        String repeatPassword = maskPasswordInput();

        if (!password.equals(repeatPassword)) {
            System.out.println("Passwords do not match. Please try again.");
            return;
        }

        // Check if the email already exists
        if (userService.emailExists(email)) {
            System.out.println("Error: Registration failed.");
            return;
        }

        try {
            userService.register(email, password);
            System.out.println("Registration successful!");
            logger.info("User registered successfully with email: {}", email);
        } catch (UserService.ServiceException e) {
            System.out.println("Error: " + e.getMessage());
            logger.error("Registration error for email {}: {}", email, e.getMessage());
        } catch (Exception e) {
            // Handle unexpected exceptions here if needed
            System.out.println("Error: " + e.getMessage());
        }

    }


    private void loginUser() {
        System.out.println("\n=== User Login ===");
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim().toLowerCase();

        // Email validation
        if (!InputValidator.isValidInput(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            System.out.println("Invalid email format. Please try again.");
            logger.warn("Invalid login attempt with email: {}", email);
            return;
        }

        if (isUserBlocked(email)) {
            System.out.println("Too many failed attempts. Please try again later.");
            return;
        }

        String password = maskPasswordInput();

        if (password.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return;
        }

        if (userService.login(email, password)) {
            System.out.println("Login successful!");
            resetFailedAttempts(email);
            postLoginMenu(email);
            logger.info("User logged in successfully: {}", email);
        } else {
            System.out.println("Login failed. Check your email and password.");
            incrementFailedAttempts(email);
            logger.warn("Failed login attempt for email: {}", email);
        }
    }

    private boolean isUserBlocked(String email) {
        if (blockedUsers.containsKey(email)) {
            Instant unblockTime = blockedUsers.get(email);
            if (Instant.now().isBefore(unblockTime)) {
                return true;
            } else {
                blockedUsers.remove(email);
            }
        }
        return false;
    }
    private void incrementFailedAttempts(String email) {
        int attempts = failedAttempts.getOrDefault(email, 0) + 1;
        failedAttempts.put(email, attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            System.out.println("Too many failed attempts. You are blocked for 5 minutes.");
            blockedUsers.put(email, Instant.now().plusMillis(BLOCK_DURATION_MS));
            failedAttempts.remove(email); // Reset failed attempts after blocking
            logger.warn("User {} blocked after {} failed attempts", email, MAX_FAILED_ATTEMPTS);
        } else {
            System.out.println("Failed attempts: " + attempts + "/" + MAX_FAILED_ATTEMPTS);
        }
    }

    private void resetFailedAttempts(String email) {
        failedAttempts.remove(email);
    }

    private String maskPasswordInput() {
        Console console = System.console();

        if (console != null) {
            char[] passwordArray = console.readPassword("Enter your password: ");
            return new String(passwordArray);
        } else {
            System.out.println("WARNING: Console is not available. Password input may not be masked in this environment.");
            StringBuilder password = new StringBuilder();

            try {
                while (true) {
                    int input = System.in.read();
                    if (input == '\n' || input == '\r') break;
                    password.append((char) input);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return password.toString().trim();
        }
    }

    private void postLoginMenu(String email) {
        boolean loggedIn = true;
        UUID userId = userService.findUserIdByEmail(email);

        while (loggedIn) {
            System.out.println("\n=== Password Manager Menu ===");
            System.out.println("1. Add Password");
            System.out.println("2. View Passwords");
            System.out.println("3. Modify Password");
            System.out.println("4. Delete Password");
            System.out.println("5. Logout");
            System.out.print("Please select an option (1-5): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());

                switch (choice) {
                    case 1:
                        addPassword(email);
                        break;
                    case 2:
                        viewPasswords(email);
                        break;
                    case 3:
                        modifyPassword(email);
                        break;
                    case 4:
                        deletePassword(email);
                        break;
                    case 5:
                        loggedIn = false;
                        System.out.println("You have logged out successfully.");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private void addPassword(String email) {
        System.out.println("\n=== Add New Password ===");
        System.out.print("Enter a description for the password: ");
        String description = scanner.nextLine().trim();

        if (!InputValidator.isValidInput(description)) {
            System.out.println("Description contains forbidden characters.");
            return;
        }
        if (description.isEmpty()) {
            System.out.println("Description cannot be empty.");
            return;
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            System.out.println("Description is too long. Maximum length is " + MAX_DESCRIPTION_LENGTH + " characters.");
            return;
        }
        if (!ALLOWED_DESCRIPTION_PATTERN.matcher(description).matches()) {
            System.out.println("Description contains invalid characters. Only letters, numbers, spaces, and .,!?_- are allowed.");
            return;
        }

        String password = maskPasswordInput().trim();

        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            System.out.println("Password must be between " + MIN_PASSWORD_LENGTH + " and " + MAX_PASSWORD_LENGTH + " characters long.");
            return;
        }

        if (!password.matches(ALLOWED_PASSWORD_PATTERN)) {
            System.out.println("Password contains invalid characters. Only letters, numbers, and the following special characters are allowed: !@#$%^&*");
            return;
        }

        if (password.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return;
        }

        try {

            userService.addPassword(email, description, password);
            System.out.println("Password added successfully!");
        } catch (Exception e) {
            System.out.println("Error adding password: " + e.getMessage());
        }
    }

    private void modifyPassword(String email) {
        System.out.println("\n=== Modify Passwords ===");

        List<PasswordEntry> passwords = userService.getPasswordsForUser(email);

        if (passwords.isEmpty()) {
            System.out.println("No passwords found for this user.");
            return;
        }

        System.out.println("Stored passwords:");
        for (PasswordEntry passwordEntry : passwords) {
            System.out.println("ID: " + passwordEntry.getId() + " | Description: " + passwordEntry.getDescription());
        }

        while (true) {
            System.out.print("\nEnter the password ID to modify, or type 'exit' to return to the main menu: ");
            String input = scanner.nextLine().trim();

            if ("exit".equalsIgnoreCase(input)) {
                System.out.println("Exiting password modification.");
                return;
            }

            try {
                UUID passwordId = UUID.fromString(input);
                PasswordEntry existingEntry = passwords.stream()
                        .filter(entry -> entry.getId().equals(passwordId))
                        .findFirst()
                        .orElse(null);

                if (existingEntry == null) {
                    System.out.println("Password ID not found.");
                    continue;
                }

                System.out.print("Enter the new description (press Enter to keep current: \"" + existingEntry.getDescription() + "\"): ");
                String newDescription = scanner.nextLine().trim();
                if (newDescription.isEmpty()) {
                    newDescription = existingEntry.getDescription(); // Retain the old description
                } else if (newDescription.length() > MAX_DESCRIPTION_LENGTH
                        || !ALLOWED_DESCRIPTION_PATTERN.matcher(newDescription).matches()) {
                    System.out.println("Invalid description. Please try again.");
                    continue;
                }

                String newPassword = maskPasswordInput().trim();

                if (newPassword.length() < MIN_PASSWORD_LENGTH || newPassword.length() > MAX_PASSWORD_LENGTH) {
                    System.out.println("Password must be between " + MIN_PASSWORD_LENGTH + " and " + MAX_PASSWORD_LENGTH + " characters long.");
                    continue;
                }

                if (!newPassword.matches(ALLOWED_PASSWORD_PATTERN)) {
                    System.out.println("Password contains invalid characters. Only letters, numbers, and the following special characters are allowed: !@#$%^&*");
                    continue;
                }



                if (newPassword.isEmpty()) {
                    System.out.println("Password cannot be empty.");
                    continue;
                }



                // Update the password
                userService.modifyPassword(passwordId, newDescription, newPassword, userService.getUserByEmail(email));
                System.out.println("Password updated successfully.");
                return;
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid UUID format. Please enter a valid password ID.");
            } catch (Exception e) {
                System.out.println("Error modifying password: " + e.getMessage());
            }
        }
    }



    private void deletePassword(String email) {
        System.out.println("\n=== Delete Password ===");
        List<PasswordEntry> passwords = userService.getPasswordsForUser(email);

        if (passwords.isEmpty()) {
            System.out.println("No passwords found for this user.");
            return;
        }

        System.out.println("Stored passwords:");
        for (PasswordEntry passwordEntry : passwords) {
            System.out.println("ID: " + passwordEntry.getId() + " | Description: " + passwordEntry.getDescription());
        }

        while (true) {
            System.out.print("\nEnter the password ID to delete, or type 'exit' to return to the main menu: ");
            String input = scanner.nextLine().trim();

            if ("exit".equalsIgnoreCase(input)) {
                System.out.println("Exiting password deletion.");
                return; // Exit without deleting
            }

            try {
                UUID passwordId = UUID.fromString(input);
                userService.deletePassword(passwordId, email);
                System.out.println("Password deleted successfully.");
                return;
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid UUID format. Please enter a valid password ID.");
            } catch (Exception e) {
                System.out.println("Error deleting password: " + e.getMessage());
            }
        }
    }



    private void viewPasswords(String email) {
        System.out.println("\n=== View Stored Passwords ===");
        List<PasswordEntry> passwords = userService.getPasswordsForUser(email);

        if (passwords.isEmpty()) {
            System.out.println("No passwords found for this user.");
            return;
        }

        System.out.println("Stored passwords:");
        for (PasswordEntry passwordEntry : passwords) {
            System.out.println("ID: " + passwordEntry.getId() + " | Description: " + passwordEntry.getDescription());
        }

        while (true) {
            System.out.print("\nEnter a password ID to copy to clipboard, or type 'exit' to return to the main menu: ");
            String input = scanner.nextLine().trim();

            if ("exit".equalsIgnoreCase(input)) {
                System.out.println("Exiting password view.");
                return;
            }

            try {
                UUID passwordId = UUID.fromString(input);
                PasswordEntry passwordEntry = passwords.stream()
                        .filter(entry -> entry.getId().equals(passwordId))
                        .findFirst()
                        .orElse(null);

                if (passwordEntry != null) {
                    String decryptedPassword = userService.decryptPassword(email, passwordEntry.getEncryptedPassword(), passwordEntry.getSalt());

                    // Copy the password to the clipboard
                    copyToClipboard(decryptedPassword);
                    System.out.println("Password copied to clipboard.");
                } else {
                    System.out.println("Invalid password ID.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid UUID format. Please enter a valid password ID.");
            } catch (Exception e) {
                System.out.println("Error decrypting password: " + e.getMessage());
            }
        }
    }

    private void copyToClipboard(String password) {
        StringSelection stringSelection = new StringSelection(password);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
    }
}