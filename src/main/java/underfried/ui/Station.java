package underfried.ui;

import java.awt.*;

/**
 * Represents a station in the restaurant (cooking station, washing station,
 * etc.)
 */
public class Station {
    private String label;
    private StationType type;
    private double x, y; // Position in tile coordinates
    private double width, height; // Size in tiles
    private boolean occupied;

    public enum StationType {
        COOKING_STATION(new Color(220, 100, 50)),
        CUTTING_STATION(new Color(150, 150, 150)),
        WASHING_STATION(new Color(100, 150, 220)),
        PREP_STATION(new Color(180, 180, 150)),
        COUNTER(new Color(139, 115, 85)),
        TABLE(new Color(139, 90, 43));

        private final Color color;

        StationType(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }
    }

    public Station(String label, StationType type, double x, double y, double width, double height) {
        this.label = label;
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.occupied = false;
    }

    public void draw(Graphics2D g2d, int tileSize) {
        int pixelX = (int) (x * tileSize);
        int pixelY = (int) (y * tileSize);
        int pixelWidth = (int) (width * tileSize);
        int pixelHeight = (int) (height * tileSize);

        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.fillRoundRect(pixelX + 3, pixelY + 3, pixelWidth, pixelHeight, 8, 8);

        // Draw station body
        g2d.setColor(type.getColor());
        g2d.fillRoundRect(pixelX, pixelY, pixelWidth, pixelHeight, 8, 8);

        // Draw border
        g2d.setColor(type.getColor().darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(pixelX, pixelY, pixelWidth, pixelHeight, 8, 8);

        // Draw occupied indicator
        if (occupied) {
            g2d.setColor(new Color(255, 200, 0, 150));
            g2d.fillOval(pixelX + 5, pixelY + 5, 10, 10);
        }
    }

    // Getters and setters
    public String getLabel() {
        return label;
    }

    public StationType getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public boolean contains(double px, double py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }
}
