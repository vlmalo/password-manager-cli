        import java.io.Console;
        import java.util.List;
        import java.util.Scanner;
        import java.util.UUID;
        import java.util.regex.Pattern;
        
        public class PasswordManagerCLI {
            private static final Scanner scanner = new Scanner(System.in);
            private final UserService userService;
        
            // Patterns and length constraints for validations
            private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
            private static final int MIN_PASSWORD_LENGTH = 8;
            private static final int MAX_EMAIL_LENGTH = 255;
            private static final int MAX_PASSWORD_HASH_LENGTH = 255;
            private static final int MAX_DESCRIPTION_LENGTH = 255;
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
        
                if (!EMAIL_PATTERN.matcher(email).matches()) {
                    System.out.println("Invalid email format. Please try again.");
                    return;
                }
                if (email.length() > MAX_EMAIL_LENGTH) {
                    System.out.println("Email is too long. Maximum length is " + MAX_EMAIL_LENGTH + " characters.");
                    return;
                }
    
    
        
                System.out.print("Enter password: ");
                String password = maskPasswordInput();
    
                if (password.length() < MIN_PASSWORD_LENGTH) {
                    System.out.println("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
                    return;
                }
                if (password.length() > MAX_PASSWORD_HASH_LENGTH) {
                    System.out.println("Password is too long. Maximum length is " + MAX_PASSWORD_HASH_LENGTH + " characters.");
                    return;
                }
    
                if (userService.emailExists(email)) {
                    System.out.println("Error: Registration failed.");
                    return;
                }
        
                try {
                    userService.register(email, password);
                    System.out.println("Registration successful!");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        
            private void loginUser() {
                System.out.println("\n=== User Login ===");
                System.out.print("Enter email: ");
                String email = scanner.nextLine().trim().toLowerCase();
        
                // Email validation
                if (!EMAIL_PATTERN.matcher(email).matches()) {
                    System.out.println("Invalid email format. Please try again.");
                    return;
                }
        
                System.out.print("Enter password: ");
                String password = maskPasswordInput();
    
                if (password.isEmpty()) {
                    System.out.println("Password cannot be empty.");
                    return;
                }
        
                if (userService.login(email, password)) {
                    System.out.println("Login successful!");
                    postLoginMenu(email);
                } else {
                    System.out.println("Login failed. Check your email and password.");
                }
            }
    
            private String maskPasswordInput() {
                Console console = System.console();
                if (console != null) {
                    char[] passwordArray = console.readPassword();
                    return new String(passwordArray);
                } else {
                    StringBuilder password = new StringBuilder();
                    try {
                        while (true) {
                            char c = (char) System.in.read();
                            if (c == '\n') break;
                            System.out.print("*");
                            password.append(c);
                        }
                    } catch (Exception e) {
                        System.out.println("Error reading password.");
                    }
                    return password.toString().trim();
                }
            }
    
    
            private void postLoginMenu(String email) {
                boolean loggedIn = true;
                int userId = userService.findUserIdByEmail(email);
        
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
        
                // Description validation
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
        
                System.out.print("Enter the password: ");
                String password = scanner.nextLine().trim();
        
                // Password validation
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
                System.out.print("Enter the password ID to modify: ");
                try {
                    String uuidString = scanner.nextLine().trim();
                    UUID passwordId = UUID.fromString(uuidString);
    
                    PasswordEntry existingEntry = userService.getPasswordsForUser(email).stream()
                            .filter(entry -> entry.getId().equals(passwordId))
                            .findFirst()
                            .orElse(null);
        
                    if (existingEntry == null) {
                        System.out.println("Password ID not found.");
                        return;
                    }
        
                    System.out.print("Enter the new description (previous: " + existingEntry.getDescription() + "): ");
                    String newDescription = scanner.nextLine().trim();
        
                    if (newDescription.isEmpty()) {
                        System.out.println("Description cannot be empty.");
                        return;
                    }
                    if (newDescription.length() > MAX_DESCRIPTION_LENGTH) {
                        System.out.println("Description is too long. Maximum length is " + MAX_DESCRIPTION_LENGTH + " characters.");
                        return;
                    }
                    if (!ALLOWED_DESCRIPTION_PATTERN.matcher(newDescription).matches()) {
                        System.out.println("Description contains invalid characters. Only letters, numbers, spaces, and .,!?_- are allowed.");
                        return;
                    }
        
                    System.out.print("Enter the new password: ");
                    String newPassword = scanner.nextLine().trim();
        
                    if (newPassword.isEmpty()) {
                        System.out.println("Password cannot be empty.");
                        return;
                    }
        
                    User user = userService.getUserByEmail(email);
                    if (user != null) {
                        userService.modifyPassword(passwordId, newDescription, newPassword, user);
                        System.out.println("Password updated successfully.");
                    } else {
                        System.out.println("User not found.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a valid number for the password ID.");
                } catch (Exception e) {
                    System.out.println("Error updating password: " + e.getMessage());
                }
            }
    
            private void deletePassword(String email) {
                System.out.println("\n=== Delete Password ===");
                System.out.print("Enter the password ID to delete: ");
                try {
                    // Parse UUID from user input
                    String uuidString = scanner.nextLine().trim();
                    UUID passwordId = UUID.fromString(uuidString);  // Convert the string input to UUID
    
                    userService.deletePassword(passwordId, email);
                    System.out.println("Password deleted successfully.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid UUID format. Please enter a valid UUID.");
                } catch (Exception e) {
                    System.out.println("Error deleting password: " + e.getMessage());
                }
            }
    
        
            private void viewPasswords(String email) {
                System.out.println("\n=== View Stored Passwords ===");
                List<PasswordEntry> passwords = userService.getPasswordsForUser(email);
                if (passwords.isEmpty()) {
                    System.out.println("No passwords found for this user.");
                } else {
                    System.out.println("------");
        
                    System.out.println("Stored passwords:");
                    for (PasswordEntry passwordEntry : passwords) {
                        try {
                            String decryptedPassword = userService.decryptPassword(email, passwordEntry.getEncryptedPassword(), passwordEntry.getSalt());
                            System.out.println("ID: " + passwordEntry.getId());
                            System.out.println("Description: " + passwordEntry.getDescription());
                            System.out.println("Password: " + decryptedPassword);
                        } catch (Exception e) {
                            System.out.println("Error decrypting password: " + e.getMessage());
                        }
                        System.out.println("------");
                    }
                }
            }
        }
