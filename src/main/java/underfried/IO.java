package underfried;

import underfried.ui.GameWindow;

/**
 * IO utility class that handles both console and UI logging
 * This allows agents to log messages that appear both in the terminal
 * and in the game's UI log panel
 */
public class IO {
    private static GameWindow gameWindow = null;

    /**
     * Set the game window for UI logging
     * 
     * @param window the GameWindow instance
     */
    public static void setGameWindow(GameWindow window) {
        gameWindow = window;
    }

    /**
     * Print a message to both console and UI
     * 
     * @param message the message to print
     */
    public static void println(String message) {
        // Always print to console (terminal)
        System.out.println(message);

        // Also send to UI if available
        if (gameWindow != null) {
            // Extract agent type from message for categorization
            String logEntry = formatLogMessage(message);
            gameWindow.appendLog(logEntry);
        }
    }

    /**
     * Print a message to both console and UI with specific agent type
     * 
     * @param agentType the type of agent (Chef, Waiter, etc.)
     * @param message   the message to print
     */
    public static void println(String agentType, String message) {
        String fullMessage = "[" + agentType + "] " + message;

        // Always print to console (terminal)
        System.out.println(fullMessage);

        // Also send to UI if available
        if (gameWindow != null) {
            gameWindow.appendLog(fullMessage);
        }
    }

    /**
     * Format log message for better display
     * Extracts agent name and formats consistently
     */
    private static String formatLogMessage(String message) {
        // If message already contains agent info, use as-is
        if (message.startsWith("[") && message.contains("]")) {
            return message;
        }

        // Try to extract agent name from JADE message format
        if (message.contains("Agent ") && message.contains(" is ready")) {
            return "[System] " + message;
        }

        // Extract agent name from common patterns
        if (message.contains("Chef:") || message.contains("chef")) {
            return "[Chef] " + message;
        } else if (message.contains("Waiter:") || message.contains("waiter")) {
            return "[Waiter] " + message;
        } else if (message.contains("DishPreparer:") || message.contains("dishPreparer")) {
            return "[DishPreparer] " + message;
        } else if (message.contains("DishWasher:") || message.contains("dishWasher")) {
            return "[DishWasher] " + message;
        } else {
            return "[System] " + message;
        }
    }

    /**
     * Check if UI logging is available
     * 
     * @return true if GameWindow is set and ready for logging
     */
    public static boolean isUILoggingAvailable() {
        return gameWindow != null;
    }
}