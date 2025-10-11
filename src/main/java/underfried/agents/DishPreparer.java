package underfried.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import underfried.Restaurant;
import underfried.ui.GameWindow;
import underfried.IO;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

enum DishPreparerState {
    COUNTER,
    DISH_PREPARING_AREA,
}

public class DishPreparer extends Agent {
    private Restaurant restaurant;
    private GameWindow gameWindow;
    private DishPreparerState currentState = DishPreparerState.DISH_PREPARING_AREA;

    // Track ingredients ready for each meal
    // Key: meal name, Value: Set of prepared ingredients
    private Map<String, Set<String>> readyIngredients;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            restaurant = (Restaurant) args[0];
            if (args.length > 1) {
                gameWindow = (GameWindow) args[1];
            }
        } else {
            throw new IllegalArgumentException("DishPreparer agent missing required arguments: Restaurant instance");
        }

        IO.println("DishPreparer", "Agent " + getAID().getName() + " is ready to prepare dishes!");
        logToUI("DishPreparer ready to assemble dishes!");

        // Initialize state
        readyIngredients = new HashMap<>();

        IO.println("DishPreparer", "Initialized with restaurant menu (" +
                restaurant.getMenuSize() + " dishes)");
        IO.println("DishPreparer", "Using restaurant plate management - Clean plates available: " +
                restaurant.cleanPlates);

        // Add behavior to handle incoming messages
        addBehaviour(new MessageHandlingBehaviour());
    }

    private void logToUI(String message) {
        if (gameWindow != null) {
            gameWindow.appendLog("[DishPreparer] " + message);
        }
    }

    @Override
    protected void takeDown() {
        IO.println("DishPreparer", "Agent " + getAID().getName() + " is finishing work.");
        IO.println("DishPreparer", "Final stats - Ready dishes: " + restaurant.readyDishes.size() +
                " (" + restaurant.readyDishes + "), Clean plates: " + restaurant.cleanPlates);
    }

    protected void goTo(DishPreparerState destination) {
        if (currentState == destination)
            return;

        double targetX = 0, targetY = 0;

        // Update UI with movement BEFORE changing state
        if (gameWindow != null) {
            switch (destination) {
                case COUNTER:
                    targetX = 9.5;
                    targetY = 7.0;
                    gameWindow.getGameState().moveAgent("dishPreparer", targetX, targetY);
                    gameWindow.getGameState().updateAgentStatus("dishPreparer", "Going to counter");
                    break;
                case DISH_PREPARING_AREA:
                    targetX = 7.5;
                    targetY = 2.5;
                    gameWindow.getGameState().moveAgent("dishPreparer", targetX, targetY);
                    gameWindow.getGameState().updateAgentStatus("dishPreparer", "Going to prep area");
                    break;
            }

            // Wait until agent has arrived at destination
            gameWindow.waitUntilArrived("dishPreparer", targetX, targetY);
        }

        // Update state AFTER arriving at destination
        currentState = destination;
    }

    private class MessageHandlingBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                String content = msg.getContent();
                IO.println("DishPreparer", "Received message: " + content);

                if (content != null && !content.trim().isEmpty()) {
                    processMessage(content, msg.getSender());
                } else {
                    IO.println("DishPreparer", "Received empty message from " + msg.getSender().getName());
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
                IO.println("DishPreparer", "Unknown message format: " + content);
            }
        } catch (Exception e) {
            IO.println("DishPreparer", "ERROR - Exception processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleIngredientReady(String content, AID sender) {
        // Move to prep area to receive the ingredient
        goTo(DishPreparerState.DISH_PREPARING_AREA);

        // Parse message format: "INGREDIENT_READY:STATUS:INGREDIENT:MEAL"
        // e.g. "INGREDIENT_READY:COOKED:meat:super_meat_boy"
        String[] parts = content.split(":");
        if (parts.length != 4) {
            IO.println("DishPreparer", "ERROR - Invalid ingredient ready format: " + content);
            return;
        }

        String status = parts[1];
        String ingredient = parts[2];
        String mealName = parts[3];

        IO.println("DishPreparer", "Received " + status.toLowerCase().replace("_", " ") +
                " " + ingredient + " for meal " + mealName);

        if (gameWindow != null) {
            gameWindow.getGameState().updateAgentStatus("dishPreparer", "Received " + ingredient + " for " + mealName);
        }

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
            IO.println("DishPreparer", "ERROR - Invalid clean plates format: " + content);
            return;
        }

        try {
            int plateCount = Integer.parseInt(parts[1]);
            restaurant.cleanPlates += plateCount;
            IO.println("DishPreparer", "Received " + plateCount + " clean plates from " +
                    sender.getName() + ". Total available: " + restaurant.cleanPlates);

            if (gameWindow != null) {
                gameWindow.getGameState().updateAgentStatus("dishPreparer", "Received " + plateCount + " clean plates");
            }

            // Try to complete pending dishes now that we have plates
            checkPendingDishes();
        } catch (NumberFormatException e) {
            IO.println("DishPreparer", "ERROR - Invalid plate count: " + parts[1]);
        }
    }

    private void checkIfDishComplete(String mealName) {
        // Get required ingredients for this meal
        String[] requiredIngredients = restaurant.getRecipe(mealName);
        if (requiredIngredients == null) {
            IO.println("DishPreparer", "ERROR - Unknown meal: " + mealName);
            return;
        }

        Set<String> required = new HashSet<>(Arrays.asList(requiredIngredients));
        Set<String> ready = readyIngredients.get(mealName);

        if (ready != null && ready.containsAll(required)) {
            IO.println("DishPreparer", "All ingredients ready for " + mealName + "!");
            IO.println("DishPreparer", "Required: " + required);
            IO.println("DishPreparer", "Ready: " + ready);

            if (gameWindow != null) {
                gameWindow.getGameState().updateAgentStatus("dishPreparer", "Ready to assemble " + mealName);
            }

            assembleDish(mealName);
        } else {
            Set<String> missing = new HashSet<>(required);
            if (ready != null) {
                missing.removeAll(ready);
            }
            IO.println("DishPreparer", "Still waiting for ingredients for " + mealName +
                    ". Missing: " + missing);

            if (gameWindow != null) {
                gameWindow.getGameState().updateAgentStatus("dishPreparer",
                        "Waiting for " + missing.size() + " ingredient(s)");
            }
        }
    }

    private void assembleDish(String mealName) {
        // Validate shared state before assembling
        IO.println("DishPreparer", "[VALIDATION] Checking resources for " + mealName);
        IO.println("DishPreparer", "[VALIDATION] Clean plates available: " + restaurant.cleanPlates);
        IO.println("DishPreparer", "[VALIDATION] Current ready dishes: " + restaurant.getReadyDishCount());

        if (restaurant.cleanPlates <= 0) {
            IO.println("DishPreparer", "[VALIDATION] ✗ Cannot assemble " + mealName + " - no clean plates available!");
            IO.println("DishPreparer", "[VALIDATION] Waiting for dishwasher to provide clean plates");
            logToUI("Waiting for clean plates to assemble " + mealName);
            return;
        }

        IO.println("DishPreparer", "[VALIDATION] ✓ Resources validated. Starting to assemble dish: " + mealName);
        logToUI("Assembling dish: " + mealName);

        if (gameWindow != null) {
            gameWindow.getGameState().updateAgentStatus("dishPreparer", "Assembling " + mealName);
        }

        int assemblyTime = restaurant.getRecipe(mealName).length * 2000; // 2 seconds per ingredient

        // Simulate dish assembly time
        try {
            Thread.sleep(assemblyTime);
        } catch (InterruptedException e) {
            IO.println("DishPreparer", "ERROR - Dish assembly interrupted for " + mealName);
            Thread.currentThread().interrupt();
            return;
        }

        // Use a clean plate and add to ready dishes queue (updates shared state)
        restaurant.cleanPlates--;
        restaurant.readyDishes.add(mealName);

        // Move to counter to place the completed dish
        goTo(DishPreparerState.COUNTER);
        logToUI("Dish ready: " + mealName + " (placed on counter)");

        if (gameWindow != null) {
            gameWindow.getGameState().updateAgentStatus("dishPreparer", "Placed " + mealName + " on counter");
        }

        // Remove ingredients from ready list since they're now used
        readyIngredients.remove(mealName);

        IO.println("DishPreparer", "SUCCESS - Completed dish: " + mealName);
        IO.println("DishPreparer", "[VALIDATION] Updated shared state - Clean plates: " + restaurant.cleanPlates);
        IO.println("DishPreparer",
                "[VALIDATION] Updated shared state - Ready dishes: " + restaurant.readyDishes.size() +
                        " (" + restaurant.readyDishes + ")");
    }

    private void checkPendingDishes() {
        // Check all pending meals to see if any can now be completed
        for (String mealName : new HashSet<>(readyIngredients.keySet())) {
            checkIfDishComplete(mealName);
        }
    }
}