package underfried.ui;

import underfried.Restaurant;
import javax.swing.*;
import java.awt.*;

/**
 * Main window for the restaurant simulation game
 */
public class GameWindow extends JFrame {
    private GamePanel gamePanel;
    private GameState gameState;
    private Restaurant restaurant;
    private JPanel controlPanel;
    private JTextArea logArea;
    private JScrollPane logScrollPane;
    private JPanel logFilterPanel;

    // Log filtering
    private JCheckBox showChef, showWaiter, showDishPreparer, showDishWasher, showSystem;
    private java.util.List<String> allLogs;

    public GameWindow(Restaurant restaurant) {
        this.restaurant = restaurant;
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

        // Create control panel
        createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);

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

    private void createControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBackground(new Color(220, 220, 220));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Restaurant info label
        JLabel infoLabel = new JLabel("Restaurant: " + restaurant.getClass().getSimpleName());
        infoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        controlPanel.add(infoLabel);

        controlPanel.add(Box.createHorizontalStrut(20));

        // Menu button
        JButton menuButton = new JButton("📋 View Menu");
        menuButton.setToolTipText("Show restaurant menu");
        menuButton.addActionListener(_ -> showMenu());
        controlPanel.add(menuButton);

        controlPanel.add(Box.createHorizontalStrut(10));

        // Status button
        JButton statusButton = new JButton("📊 Show Status");
        statusButton.setToolTipText("Show detailed restaurant status");
        statusButton.addActionListener(_ -> showStatus());
        controlPanel.add(statusButton);

        controlPanel.add(Box.createHorizontalStrut(10));

        // Help button
        JButton helpButton = new JButton("❓ Help");
        helpButton.setToolTipText("Show game information");
        helpButton.addActionListener(_ -> showHelp());
        controlPanel.add(helpButton);
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

    private void showMenu() {
        StringBuilder menuText = new StringBuilder();
        menuText.append("=== Underfried Restaurant Menu ===\n\n");

        for (String dish : restaurant.getAvailableDishes()) {
            String[] ingredients = restaurant.getRecipe(dish);
            menuText.append("• ").append(dish.toUpperCase()).append("\n");
            menuText.append("  Ingredients: ").append(String.join(", ", ingredients)).append("\n\n");
        }

        JTextArea textArea = new JTextArea(menuText.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 12));
        textArea.setBackground(new Color(255, 250, 240));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "Restaurant Menu",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showStatus() {
        StringBuilder statusText = new StringBuilder();
        statusText.append("=== Restaurant Status ===\n\n");
        statusText.append("Plates:\n");
        statusText.append("  • Clean Plates: ").append(restaurant.cleanPlates).append("\n");
        statusText.append("  • Taken Plates: ").append(restaurant.takenPlates).append("\n");
        statusText.append("  • Dirty Plates: ").append(restaurant.dirtyPlates).append("\n\n");

        statusText.append("Orders:\n");
        statusText.append("  • Pending Orders: ").append(restaurant.getPendingOrderCount()).append("\n");
        statusText.append("  • Ready Dishes: ").append(restaurant.getReadyDishCount()).append("\n\n");

        statusText.append("Agents:\n");
        for (AgentSprite agent : gameState.getAgents()) {
            statusText.append("  • ").append(agent.getAgentName())
                    .append(" (").append(agent.getType()).append("): ")
                    .append(agent.getStatus()).append("\n");
        }

        JTextArea textArea = new JTextArea(statusText.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(350, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "Restaurant Status",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showHelp() {
        String helpText = "=== Underfried Restaurant Game ===\n\n" +
                "This is a top-down view of a restaurant simulation.\n\n" +
                "AGENTS (NPCs):\n" +
                "• Chef (Red) 👨‍🍳 - Cooks and cuts ingredients\n" +
                "• Waiter (Blue) 👔 - Takes orders and serves dishes\n" +
                "• Dish Preparer (Green) 🍽️ - Assembles final dishes\n" +
                "• Dish Washer (Yellow) 🧼 - Cleans dirty plates\n\n" +
                "AREAS:\n" +
                "• Left side: Kitchen with cooking stations\n" +
                "• Right side: Dining area with tables\n" +
                "• Center: Pass-through counter\n\n" +
                "GAMEPLAY:\n" +
                "The simulation runs automatically using JADE agents.\n" +
                "Watch as agents coordinate to prepare and serve meals!\n\n" +
                "CONTROLS:\n" +
                "• View Menu: See available dishes\n" +
                "• Show Status: See detailed restaurant stats\n" +
                "• Activity Log: Monitor agent actions (right panel)";

        JTextArea textArea = new JTextArea(helpText);
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 12));
        textArea.setBackground(new Color(255, 255, 230));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(450, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "Game Help",
                JOptionPane.INFORMATION_MESSAGE);
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

    public GameState getGameState() {
        return gameState;
    }

    public void cleanup() {
        if (gamePanel != null) {
            gamePanel.stopGameLoop();
        }
    }
}
