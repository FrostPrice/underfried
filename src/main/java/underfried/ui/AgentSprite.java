package underfried.ui;

import java.awt.*;

/**
 * Visual representation of an agent (NPC) in the game
 */
public class AgentSprite {
    private String agentName;
    private AgentType type;
    private double x, y; // Position in tile coordinates
    private double targetX, targetY; // Target position for movement
    private double speed = 0.05; // Movement speed (tiles per frame)
    private String status; // Current activity status
    private Color color;

    public enum AgentType {
        CHEF(new Color(255, 100, 100), "👨‍🍳"),
        WAITER(new Color(100, 100, 255), "👔"),
        DISH_PREPARER(new Color(100, 255, 100), "🍽️"),
        DISH_WASHER(new Color(255, 255, 100), "🧼");

        private final Color color;
        private final String emoji;

        AgentType(Color color, String emoji) {
            this.color = color;
            this.emoji = emoji;
        }

        public Color getColor() {
            return color;
        }

        public String getEmoji() {
            return emoji;
        }
    }

    public AgentSprite(String agentName, AgentType type, double startX, double startY) {
        this.agentName = agentName;
        this.type = type;
        this.x = startX;
        this.y = startY;
        this.targetX = startX;
        this.targetY = startY;
        this.color = type.getColor();
        this.status = "Idle";
    }

    public void update() {
        // Smooth movement towards target
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.05) { // Increased threshold to prevent micro-adjustments
            double moveAmount = Math.min(speed, distance); // Don't overshoot
            x += (dx / distance) * moveAmount;
            y += (dy / distance) * moveAmount;
        } else {
            // Snap to target when very close to prevent oscillation
            x = targetX;
            y = targetY;
        }
    }

    public void draw(Graphics2D g2d, int tileSize) {
        int pixelX = (int) (x * tileSize);
        int pixelY = (int) (y * tileSize);
        int size = tileSize - 10;

        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillOval(pixelX + 2, pixelY + size - 5, size, 8);

        // Draw agent body (circle)
        g2d.setColor(color);
        g2d.fillOval(pixelX, pixelY, size, size);

        // Draw outline
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(pixelX, pixelY, size, size);

        // Draw agent icon/emoji
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        FontMetrics fm = g2d.getFontMetrics();
        String emoji = type.getEmoji();
        int emojiWidth = fm.stringWidth(emoji);
        int emojiHeight = fm.getAscent();
        g2d.drawString(emoji, pixelX + (size - emojiWidth) / 2, pixelY + (size + emojiHeight) / 2 - 2);

        // Draw agent name below
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 9));
        fm = g2d.getFontMetrics();
        String displayName = agentName;
        int nameWidth = fm.stringWidth(displayName);
        g2d.drawString(displayName, pixelX + (size - nameWidth) / 2, pixelY + size + 12);

        // Draw status (if not idle)
        if (status != null && !status.equals("Idle")) {
            g2d.setFont(new Font("Arial", Font.PLAIN, 8));
            fm = g2d.getFontMetrics();
            int statusWidth = fm.stringWidth(status);
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.fillRoundRect(pixelX + (size - statusWidth) / 2 - 3, pixelY - 15, statusWidth + 6, 12, 4, 4);
            g2d.setColor(Color.BLACK);
            g2d.drawString(status, pixelX + (size - statusWidth) / 2, pixelY - 6);
        }
    }

    // Getters and setters
    public String getAgentName() {
        return agentName;
    }

    public AgentType getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setTargetPosition(double x, double y) {
        this.targetX = x;
        this.targetY = y;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isMoving() {
        double dx = targetX - x;
        double dy = targetY - y;
        return Math.sqrt(dx * dx + dy * dy) > 0.05; // Match the threshold in update()
    }
}
