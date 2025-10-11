package underfried.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main game panel that renders the top-down view of the restaurant
 */
public class GamePanel extends JPanel implements ActionListener {
    private static final int TILE_SIZE = 40; // Size of each tile in pixels
    private static final int GRID_WIDTH = 20; // Number of tiles horizontally
    private static final int GRID_HEIGHT = 15; // Number of tiles vertically

    private Timer gameTimer;
    private GameState gameState;

    // Color scheme
    private static final Color FLOOR_COLOR = new Color(230, 220, 200);
    private static final Color WALL_COLOR = new Color(80, 60, 40);
    private static final Color COUNTER_COLOR = new Color(139, 115, 85);
    private static final Color KITCHEN_FLOOR = new Color(240, 240, 240);
    private static final Color DINING_FLOOR = new Color(250, 235, 215);

    public GamePanel(GameState gameState) {
        this.gameState = gameState;

        setPreferredSize(new Dimension(GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE));
        setBackground(FLOOR_COLOR);
        setDoubleBuffered(true);

        // Game loop timer (60 FPS)
        gameTimer = new Timer(1000 / 60, this);
        gameTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Update game state
        gameState.update();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable anti-aliasing for smoother graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw floor tiles
        drawFloor(g2d);

        // Draw walls and room divisions
        drawWalls(g2d);

        // Draw kitchen stations
        drawStations(g2d);

        // Draw objects (plates, food items)
        drawObjects(g2d);

        // Draw agents (NPCs)
        drawAgents(g2d);

        // Draw UI overlays (status, labels)
        drawOverlay(g2d);
    }

    private void drawFloor(Graphics2D g2d) {
        // Kitchen area (left side)
        g2d.setColor(KITCHEN_FLOOR);
        g2d.fillRect(0, 0, TILE_SIZE * 10, TILE_SIZE * GRID_HEIGHT);

        // Dining area (right side)
        g2d.setColor(DINING_FLOOR);
        g2d.fillRect(TILE_SIZE * 10, 0, TILE_SIZE * 10, TILE_SIZE * GRID_HEIGHT);

        // Draw grid lines (subtle)
        g2d.setColor(new Color(0, 0, 0, 20));
        for (int x = 0; x <= GRID_WIDTH; x++) {
            g2d.drawLine(x * TILE_SIZE, 0, x * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);
        }
        for (int y = 0; y <= GRID_HEIGHT; y++) {
            g2d.drawLine(0, y * TILE_SIZE, GRID_WIDTH * TILE_SIZE, y * TILE_SIZE);
        }
    }

    private void drawWalls(Graphics2D g2d) {
        g2d.setColor(WALL_COLOR);

        // Outer walls
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(0, 0, GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);

        // Divider between kitchen and dining area
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(TILE_SIZE * 10, 0, TILE_SIZE * 10, GRID_HEIGHT * TILE_SIZE);

        // Counter between kitchen and dining (pass-through window)
        g2d.setColor(COUNTER_COLOR);
        int counterY = TILE_SIZE * 6;
        g2d.fillRect(TILE_SIZE * 9, counterY, TILE_SIZE * 2, TILE_SIZE * 3);

        // Pass-through window
        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(TILE_SIZE * 10 - 5, counterY + 10, 10, TILE_SIZE * 3 - 20);
    }

    private void drawStations(Graphics2D g2d) {
        for (Station station : gameState.getStations()) {
            station.draw(g2d, TILE_SIZE);
        }
    }

    private void drawObjects(Graphics2D g2d) {
        for (GameObject obj : gameState.getObjects()) {
            obj.draw(g2d, TILE_SIZE);
        }
    }

    private void drawAgents(Graphics2D g2d) {
        for (AgentSprite agent : gameState.getAgents()) {
            agent.draw(g2d, TILE_SIZE);
        }
    }

    private void drawOverlay(Graphics2D g2d) {
        // Draw status information at the top
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, GRID_WIDTH * TILE_SIZE, 30);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));

        // Display plate counts
        String status = String.format("Clean Plates: %d | Dirty Plates: %d | Pending Orders: %d | Ready Dishes: %d",
                gameState.getRestaurant().cleanPlates,
                gameState.getRestaurant().dirtyPlates,
                gameState.getRestaurant().getPendingOrderCount(),
                gameState.getRestaurant().getReadyDishCount());

        g2d.drawString(status, 10, 20);

        // Draw station labels
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        for (Station station : gameState.getStations()) {
            if (station.getLabel() != null) {
                int x = (int) (station.getX() * TILE_SIZE);
                int y = (int) (station.getY() * TILE_SIZE - 5);
                g2d.setColor(Color.BLACK);
                g2d.drawString(station.getLabel(), x + 2, y);
            }
        }
    }

    public void stopGameLoop() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }
}
