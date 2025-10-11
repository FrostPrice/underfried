package underfried.ui;

import java.awt.*;

/**
 * Represents an interactive object in the game (plates, food items, etc.)
 */
public class GameObject {
    private String name;
    private ObjectType type;
    private double x, y; // Position in tile coordinates
    private boolean visible;

    public enum ObjectType {
        CLEAN_PLATE(new Color(255, 255, 255), "🍽️"),
        DIRTY_PLATE(new Color(150, 120, 100), "🍽️"),
        FOOD_READY(new Color(255, 200, 100), "🍔"),
        INGREDIENT(new Color(100, 200, 100), "🥕"),
        FIRE(new Color(255, 100, 0), "🔥"),
        BURNED_FOOD(new Color(50, 40, 30), "🔥"),
        RAT(new Color(128, 128, 128), "🐀");

        private final Color color;
        private final String icon;

        ObjectType(Color color, String icon) {
            this.color = color;
            this.icon = icon;
        }

        public Color getColor() {
            return color;
        }

        public String getIcon() {
            return icon;
        }
    }

    public GameObject(String name, ObjectType type, double x, double y) {
        this.name = name;
        this.type = type;
        this.x = x;
        this.y = y;
        this.visible = true;
    }

    public void draw(Graphics2D g2d, int tileSize) {
        if (!visible)
            return;

        int pixelX = (int) (x * tileSize);
        int pixelY = (int) (y * tileSize);
        int size = tileSize / 2;

        // Draw object background circle
        g2d.setColor(type.getColor());
        g2d.fillOval(pixelX, pixelY, size, size);

        // Draw border
        g2d.setColor(type.getColor().darker());
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(pixelX, pixelY, size, size);

        // Draw icon
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, size - 4));
        FontMetrics fm = g2d.getFontMetrics();
        String icon = type.getIcon();
        int iconWidth = fm.stringWidth(icon);
        int iconHeight = fm.getAscent();
        g2d.drawString(icon,
                pixelX + (size - iconWidth) / 2,
                pixelY + (size + iconHeight) / 2 - 2);
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public ObjectType getType() {
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

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
