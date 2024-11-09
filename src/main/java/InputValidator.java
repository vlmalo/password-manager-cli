public class InputValidator {

    public static boolean isValidInput(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        String[] forbiddenPatterns = {"exit", "quit", "..", "/", "\\", "file:"};
        for (String pattern : forbiddenPatterns) {
            if (input.toLowerCase().contains(pattern)) {
                return false;
            }
        }
        return true;
    }

}
