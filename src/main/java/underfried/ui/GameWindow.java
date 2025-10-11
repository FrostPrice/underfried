package underfried.ui;

import underfried.Restaurant;
import javax.swing.*;
import java.awt.*;
import underfried.IO;

/**
 * Main window for the restaurant simulation game
 */
public class GameWindow extends JFrame {
    private GamePanel gamePanel;
    private GameState gameState;
    private JTextArea logArea;
    private JScrollPane logScrollPane;
    private JPanel logFilterPanel;

    // Log filtering
    private JCheckBox showChef, showWaiter, showDishPreparer, showDishWasher, showSystem;
    private java.util.List<String> allLogs;

    public GameWindow(Restaurant restaurant) {
        this.gameState = new GameState(restaurant);
        this.allLogs = new java.util.ArrayList<>();

        initializeUI();
    }

    private void initializeUI() {
        setTitle("Underfried Restaurant - Top Down View");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create game panel
        gamePanel = new GamePanel(gameState);
        add(gamePanel, BorderLayout.CENTER);

        // Create side panel with log and filters
        createSidePanel();

        // Create main log panel with filters
        JPanel logMainPanel = new JPanel(new BorderLayout());
        logMainPanel.add(createLogFilterPanel(), BorderLayout.NORTH);
        logMainPanel.add(logScrollPane, BorderLayout.CENTER);
        add(logMainPanel, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    private void createSidePanel() {
        logArea = new JTextArea(35, 30);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setBackground(new Color(40, 40, 40));
        logArea.setForeground(new Color(0, 255, 0));
        logArea.setText("=== Restaurant Activity Log ===\n");
        logArea.append("System started...\n");
        logArea.append("Agents initialized.\n");
        logArea.append("\n");

        // Add initial logs to the list
        allLogs.add("=== Restaurant Activity Log ===");
        allLogs.add("System started...");
        allLogs.add("Agents initialized.");
        allLogs.add("");

        logScrollPane = new JScrollPane(logArea);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    private JPanel createLogFilterPanel() {
        logFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logFilterPanel.setBackground(new Color(50, 50, 50));
        logFilterPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Log Filters",
                0, 0,
                new Font("Arial", Font.BOLD, 10),
                Color.WHITE));

        // Create checkboxes for each agent type
        showSystem = new JCheckBox("System", true);
        showChef = new JCheckBox("Chef", true);
        showWaiter = new JCheckBox("Waiter", true);
        showDishPreparer = new JCheckBox("DishPreparer", true);
        showDishWasher = new JCheckBox("DishWasher", true);

        // Style checkboxes
        JCheckBox[] checkboxes = { showSystem, showChef, showWaiter, showDishPreparer, showDishWasher };
        for (JCheckBox cb : checkboxes) {
            cb.setBackground(new Color(50, 50, 50));
            cb.setForeground(Color.WHITE);
            cb.setFont(new Font("Arial", Font.PLAIN, 9));
            cb.addActionListener(_ -> updateLogDisplay());
            logFilterPanel.add(cb);
        }

        // Add "All" and "None" buttons
        JButton allButton = new JButton("All");
        allButton.setFont(new Font("Arial", Font.PLAIN, 9));
        allButton.addActionListener(_ -> {
            for (JCheckBox cb : checkboxes) {
                cb.setSelected(true);
            }
            updateLogDisplay();
        });

        JButton noneButton = new JButton("None");
        noneButton.setFont(new Font("Arial", Font.PLAIN, 9));
        noneButton.addActionListener(_ -> {
            for (JCheckBox cb : checkboxes) {
                cb.setSelected(false);
            }
            updateLogDisplay();
        });

        logFilterPanel.add(allButton);
        logFilterPanel.add(noneButton);

        return logFilterPanel;
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            // Add to the full log list
            allLogs.add(message);

            // Update display based on current filters
            updateLogDisplay();
        });
    }

    private void updateLogDisplay() {
        StringBuilder filteredLog = new StringBuilder();

        for (String log : allLogs) {
            if (shouldShowLog(log)) {
                filteredLog.append(log).append("\n");
            }
        }

        logArea.setText(filteredLog.toString());
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private boolean shouldShowLog(String log) {
        if (log.startsWith("[System]") || log.equals("=== Restaurant Activity Log ===") ||
                log.equals("System started...") || log.equals("Agents initialized...") || log.isEmpty()) {
            return showSystem.isSelected();
        } else if (log.startsWith("[Chef]") || log.contains("Chef")) {
            return showChef.isSelected();
        } else if (log.startsWith("[Waiter]") || log.contains("Waiter")) {
            return showWaiter.isSelected();
        } else if (log.startsWith("[DishPreparer]") || log.contains("DishPreparer")) {
            return showDishPreparer.isSelected();
        } else if (log.startsWith("[DishWasher]") || log.contains("DishWasher")) {
            return showDishWasher.isSelected();
        } else {
            // Default to System for unrecognized logs
            return showSystem.isSelected();
        }
    }

    public void wait(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void waitUntilArrived(String agentName, double targetX, double targetY) {
        final double ARRIVAL_THRESHOLD = 0.1; // Consider arrived if within 0.1 units
        final int MAX_WAIT_TIME = 10000; // Maximum 10 seconds wait
        final int CHECK_INTERVAL = 100; // Check every 100ms

        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < MAX_WAIT_TIME) {
            // Get current agent position from GameState
            double currentX = this.getGameState().getAgent(agentName).getX();
            double currentY = this.getGameState().getAgent(agentName).getY();

            // Calculate distance to target
            double distance = Math.sqrt(
                    Math.pow(targetX - currentX, 2) +
                            Math.pow(targetY - currentY, 2));

            // Check if arrived
            if (distance < ARRIVAL_THRESHOLD) {
                String capitalizedName = agentName.substring(0, 1).toUpperCase() + agentName.substring(1);
                IO.println(capitalizedName, "Arrived at destination (" + targetX + ", " + targetY + ")");
                return;
            }

            // Wait a bit before checking again
            wait(CHECK_INTERVAL);
        }

        // Timeout - agent took too long to arrive
        IO.println("Chef", "WARNING - Timeout waiting to arrive at (" + targetX + ", " + targetY + ")");
    }

    public GameState getGameState() {
        return gameState;
    }

    public void cleanup() {
        if (gamePanel != null) {
            gamePanel.stopGameLoop();
        }
    }
}
