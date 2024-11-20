import java.util.regex.Pattern;

public class InputValidator {

    private static final String[] FORBIDDEN_PATTERNS = {"exit", "quit", "..", "/", "\\", "file:"};
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public static boolean isValidInput(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }

        // Check for forbidden patterns
        for (String pattern : FORBIDDEN_PATTERNS) {
            if (input.toLowerCase().contains(pattern)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidEmail(String input) {
        return EMAIL_PATTERN.matcher(input).matches();
    }
}
