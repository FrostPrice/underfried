package underfried;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Restaurant {
    public Restaurant(String name) {
        this.restaurantName = name;
        this.menu = new HashMap<>();
        initializeMenu();
    }

    public int cleanPlates = 10;
    public int takenPlates = 10;
    public int dirtyPlates = 0;

    public Queue<String> readyDishes = new LinkedList<>() {
        {
            add("steak");
            add("pasta");
            add("salad");
        }
    };

    private Map<String, String[]> menu;
    private String restaurantName;

    /**
     * Initialize the restaurant menu with available dishes and their recipes
     */
    private void initializeMenu() {
        // Main dishes
        menu.put("super_meat_boy", new String[] { "meat", "tomato", "onion" });
        menu.put("super_chicken_boy", new String[] { "chicken", "lettuce", "carrot" });
        menu.put("steak", new String[] { "meat", "potato" });
        menu.put("fish", new String[] { "fish", "carrot", "potato" });

        // Pasta dishes
        menu.put("pasta", new String[] { "pasta", "tomato" });
        menu.put("carbonara", new String[] { "pasta", "meat", "onion" });

        // Salads
        menu.put("salad", new String[] { "lettuce", "tomato", "onion" });
        menu.put("caesar", new String[] { "lettuce", "chicken", "tomato" });

        // Soups
        menu.put("soup", new String[] { "carrot", "potato", "onion" });
        menu.put("chicken_soup", new String[] { "chicken", "carrot", "potato" });
    }

    /**
     * Get the recipe for a specific dish
     * 
     * @param dishName the name of the dish
     * @return array of ingredients needed, or null if dish doesn't exist
     */
    public String[] getRecipe(String dishName) {
        return menu.get(dishName.toLowerCase());
    }

    /**
     * Get all available dishes in the menu
     * 
     * @return a set of all dish names
     */
    public java.util.Set<String> getAvailableDishes() {
        return menu.keySet();
    }

    /**
     * Get the total number of dishes in the menu
     * 
     * @return number of dishes available
     */
    public int getMenuSize() {
        return menu.size();
    }

    /**
     * Print the entire menu to console
     */
    public void printMenu() {
        System.out.println("=== " + restaurantName + " Menu ===");
        for (Map.Entry<String, String[]> entry : menu.entrySet()) {
            System.out.println(entry.getKey() + ": " + String.join(", ", entry.getValue()));
        }
        System.out.println("Total dishes: " + menu.size());
    }
}
