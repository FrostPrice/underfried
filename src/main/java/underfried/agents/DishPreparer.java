package underfried.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import underfried.Restaurant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class DishPreparer extends Agent {
    private Restaurant restaurant;

    // Track ingredients ready for each meal
    // Key: meal name, Value: Set of prepared ingredients
    private Map<String, Set<String>> readyIngredients;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            restaurant = (Restaurant) args[0];
        } else {
            throw new IllegalArgumentException("DishPreparer agent missing required arguments: Restaurant instance");
        }

        System.out.println("DishPreparer Agent " + getAID().getName() + " is ready to prepare dishes!");

        // Initialize state
        readyIngredients = new HashMap<>();

        System.out.println("DishPreparer: Initialized with restaurant menu (" +
                restaurant.getMenuSize() + " dishes)");
        System.out.println("DishPreparer: Using restaurant plate management - Clean plates available: " +
                restaurant.cleanPlates);

        // Add behavior to handle incoming messages
        addBehaviour(new MessageHandlingBehaviour());
    }

    @Override
    protected void takeDown() {
        System.out.println("DishPreparer Agent " + getAID().getName() + " is finishing work.");
        System.out.println("DishPreparer: Final stats - Ready dishes: " + restaurant.readyDishes.size() +
                " (" + restaurant.readyDishes + "), Clean plates: " + restaurant.cleanPlates);
    }

    private class MessageHandlingBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                String content = msg.getContent();
                System.out.println("DishPreparer received message: " + content);

                if (content != null && !content.trim().isEmpty()) {
                    processMessage(content, msg.getSender());
                } else {
                    System.out.println("DishPreparer received empty message from " + msg.getSender().getName());
                }
            } else {
                block();
            }
        }
    }

    private void processMessage(String content, AID sender) {
        try {
            if (content.startsWith("INGREDIENT_READY:")) {
                handleIngredientReady(content, sender);
            } else if (content.startsWith("CLEAN_PLATES:")) {
                handleCleanPlates(content, sender);
            } else {
                System.out.println("DishPreparer: Unknown message format: " + content);
            }
        } catch (Exception e) {
            System.out.println("DishPreparer: ERROR - Exception processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleIngredientReady(String content, AID sender) {
        // Parse message format: "INGREDIENT_READY:STATUS:INGREDIENT:MEAL"
        // e.g. "INGREDIENT_READY:COOKED:meat:super_meat_boy"
        String[] parts = content.split(":");
        if (parts.length != 4) {
            System.out.println("DishPreparer: ERROR - Invalid ingredient ready format: " + content);
            return;
        }

        String status = parts[1];
        String ingredient = parts[2];
        String mealName = parts[3];

        System.out.println("DishPreparer: Received " + status.toLowerCase().replace("_", " ") +
                " " + ingredient + " for meal " + mealName);

        // Add ingredient to ready list for this meal
        if (!readyIngredients.containsKey(mealName)) {
            readyIngredients.put(mealName, new HashSet<>());
        }
        readyIngredients.get(mealName).add(ingredient);

        // Check if all ingredients for this meal are ready
        checkIfDishComplete(mealName);
    }

    private void handleCleanPlates(String content, AID sender) {
        // Parse message format: "CLEAN_PLATES:COUNT"
        // e.g. "CLEAN_PLATES:3"
        String[] parts = content.split(":");
        if (parts.length != 2) {
            System.out.println("DishPreparer: ERROR - Invalid clean plates format: " + content);
            return;
        }

        try {
            int plateCount = Integer.parseInt(parts[1]);
            restaurant.cleanPlates += plateCount;
            System.out.println("DishPreparer: Received " + plateCount + " clean plates from " +
                    sender.getName() + ". Total available: " + restaurant.cleanPlates);

            // Try to complete pending dishes now that we have plates
            checkPendingDishes();
        } catch (NumberFormatException e) {
            System.out.println("DishPreparer: ERROR - Invalid plate count: " + parts[1]);
        }
    }

    private void checkIfDishComplete(String mealName) {
        // Get required ingredients for this meal
        String[] requiredIngredients = restaurant.getRecipe(mealName);
        if (requiredIngredients == null) {
            System.out.println("DishPreparer: ERROR - Unknown meal: " + mealName);
            return;
        }

        Set<String> required = new HashSet<>(Arrays.asList(requiredIngredients));
        Set<String> ready = readyIngredients.get(mealName);

        if (ready != null && ready.containsAll(required)) {
            System.out.println("DishPreparer: All ingredients ready for " + mealName + "!");
            System.out.println("DishPreparer: Required: " + required);
            System.out.println("DishPreparer: Ready: " + ready);

            assembleDish(mealName);
        } else {
            Set<String> missing = new HashSet<>(required);
            if (ready != null) {
                missing.removeAll(ready);
            }
            System.out.println("DishPreparer: Still waiting for ingredients for " + mealName +
                    ". Missing: " + missing);
        }
    }

    private void assembleDish(String mealName) {
        if (restaurant.cleanPlates <= 0) {
            System.out.println("DishPreparer: Cannot assemble " + mealName + " - no clean plates available!");
            System.out.println("DishPreparer: Waiting for dishwasher to provide clean plates");
            return;
        }

        System.out.println("DishPreparer: Starting to assemble dish: " + mealName);

        int assemblyTime = restaurant.getRecipe(mealName).length * 2000; // 2 seconds per ingredient

        // Simulate dish assembly time
        try {
            Thread.sleep(assemblyTime);
        } catch (InterruptedException e) {
            System.out.println("DishPreparer: ERROR - Dish assembly interrupted for " + mealName);
            Thread.currentThread().interrupt();
            return;
        }

        // Use a clean plate and add to ready dishes queue
        restaurant.cleanPlates--;
        restaurant.readyDishes.add(mealName);

        // Remove ingredients from ready list since they're now used
        readyIngredients.remove(mealName);

        System.out.println("DishPreparer: SUCCESS - Completed dish: " + mealName);
        System.out.println("DishPreparer: Used 1 clean plate. Remaining plates: " + restaurant.cleanPlates);
        System.out
                .println("DishPreparer: Dish ready for service. Total ready dishes: " + restaurant.readyDishes.size());
    }

    private void checkPendingDishes() {
        // Check all pending meals to see if any can now be completed
        for (String mealName : new HashSet<>(readyIngredients.keySet())) {
            checkIfDishComplete(mealName);
        }
    }

    // Utility method to get current status
    public void printStatus() {
        System.out.println("=== DishPreparer Status ===");
        System.out.println("Available clean plates: " + restaurant.cleanPlates);
        System.out.println("Ready dishes: " + restaurant.readyDishes.size() + " (" + restaurant.readyDishes + ")");
        System.out.println("Pending meals: " + readyIngredients.size());

        if (!readyIngredients.isEmpty()) {
            System.out.println("Pending meal details:");
            for (Map.Entry<String, Set<String>> entry : readyIngredients.entrySet()) {
                String meal = entry.getKey();
                Set<String> ready = entry.getValue();
                String[] required = restaurant.getRecipe(meal);
                Set<String> requiredSet = new HashSet<>(Arrays.asList(required));
                Set<String> missing = new HashSet<>(requiredSet);
                missing.removeAll(ready);

                System.out.println("  " + meal + " - Ready: " + ready + ", Missing: " + missing);
            }
        }
        System.out.println("========================");
    }
}