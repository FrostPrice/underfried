package underfried.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import underfried.Restaurant;
import underfried.ChefKnowledge;
import underfried.ui.GameWindow;
import underfried.IO;

public class Chef extends Agent {
    private Restaurant restaurant;
    private ChefKnowledge chefKnowledge;
    private GameWindow gameWindow;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            restaurant = (Restaurant) args[0];
            if (args.length > 1) {
                gameWindow = (GameWindow) args[1];
            }
        } else {
            throw new IllegalArgumentException("Chef agent missing required arguments: Restaurant instance");
        }

        IO.println("Chef", "Agent " + getAID().getName() + " is ready to cook!");
        logToUI("Chef ready to cook!");

        // Initialize chef knowledge
        chefKnowledge = new ChefKnowledge(getAID().getLocalName());

        IO.println("Chef", "Initialized with restaurant menu (" +
                restaurant.getMenuSize() + " dishes)");
        IO.println("Chef", "Ready with cooking knowledge for " +
                chefKnowledge.getCookableIngredients().size()
                + " ingredients");

        // Add behavior to handle orders from the restaurant queue
        addBehaviour(new OrderHandlingBehaviour());
    }

    private void logToUI(String message) {
        if (gameWindow != null) {
            gameWindow.appendLog("[Chef] " + message);
        }
    }

    @Override
    protected void takeDown() {
        IO.println("Chef", "Agent " + getAID().getName() + " is finishing work.");
    }

    private class OrderHandlingBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            // Receive orders via ACL messages
            ACLMessage msg = receive();
            if (msg != null) {
                IO.println("Chef", "Received order: " + msg.getContent());

                // Parse the order content
                String orderContent = msg.getContent();
                if (orderContent != null && !orderContent.trim().isEmpty()) {
                    // Validate against shared state
                    int expectedOrders = restaurant.getPendingOrderCount();
                    IO.println("Chef", "[VALIDATION] Pending orders in queue: " + expectedOrders);

                    // Process orders from message
                    // Parse order format: "PLATE1\nPLATE2\n..." where each meal is on a new line
                    String[] meals = orderContent.split("\n");
                    IO.println("Chef", "Processing " + meals.length + " order(s) from message");

                    // Process each meal in the order
                    for (String meal : meals) {
                        meal = meal.trim();
                        if (!meal.isEmpty()) {
                            // Validate this order exists in shared state
                            String queuedOrder = restaurant.getNextOrder();
                            if (queuedOrder != null && queuedOrder.equalsIgnoreCase(meal)) {
                                IO.println("Chef", "[VALIDATION] ✓ Order '" + meal +
                                        "' matches queued order '" + queuedOrder + "'");
                                processMeal(meal);
                            } else if (queuedOrder != null) {
                                IO.println("Chef", "[VALIDATION] ⚠ WARNING - Message order '" + meal +
                                        "' doesn't match queued order '" + queuedOrder + "'");
                                // Process anyway but log discrepancy
                                processMeal(meal);
                            } else {
                                IO.println("Chef", "[VALIDATION] ⚠ WARNING - No queued order found for '" +
                                        meal + "' but processing from message");
                                processMeal(meal);
                            }
                        }
                    }

                    IO.println("Chef", "[VALIDATION] Remaining orders in queue: " +
                            restaurant.getPendingOrderCount());
                } else {
                    IO.println("Chef", "ERROR - Received empty order message");
                }
            } else {
                block();
            }
        }
    }

    private void processMeal(String mealName) {
        IO.println("Chef", "Starting to prepare ingredients for meal: " + mealName);
        logToUI("Processing order: " + mealName);

        if (gameWindow != null) {
            gameWindow.getGameState().updateAgentStatus("chef", "Preparing " + mealName);
        }

        // Get the recipe from the restaurant menu
        String[] ingredients = restaurant.getRecipe(mealName);
        if (ingredients == null) {
            IO.println("Chef", "ERROR - Unknown meal: " + mealName);
            IO.println("Chef", "Available meals: " + restaurant.getAvailableDishes());
            logToUI("ERROR: Unknown meal - " + mealName);
            return;
        }

        // Process each ingredient for the meal independently
        for (String ingredient : ingredients) {
            ingredient = ingredient.trim().toLowerCase();
            processIngredient(ingredient, mealName);
        }

        IO.println("Chef", "Finished processing all ingredients for meal: " + mealName);
        logToUI("Completed: " + mealName);
    }

    private void processIngredient(String ingredient, String mealName) {
        IO.println("Chef", "Processing ingredient " + ingredient + " for meal " + mealName);

        // Check what processing this ingredient actually needs
        boolean needsCutting = chefKnowledge.needsCutting(ingredient);
        boolean needsCooking = chefKnowledge.needsCooking(ingredient);
        boolean shouldCook = chefKnowledge.shouldCookForDish(ingredient, mealName);

        boolean cutSuccess = true;
        boolean cookSuccess = true;

        // Cut the ingredient if needed
        if (needsCutting) {
            cutSuccess = cutIngredient(ingredient);
            if (!cutSuccess) {
                IO.println("Chef", "Failed to cut " + ingredient + " for meal " + mealName);
                return; // Don't proceed to cooking if cutting failed
            }
        } else {
            IO.println("Chef", ingredient + " doesn't need cutting for this dish");
        }

        // Cook the ingredient if needed and if cutting was successful (or not required)
        if (cutSuccess && needsCooking && shouldCook) {
            cookSuccess = cookIngredient(ingredient);
            if (!cookSuccess) {
                IO.println("Chef", "Failed to cook " + ingredient + " for meal " + mealName);
                return; // Don't notify if cooking failed
            }
        } else if (needsCooking && !shouldCook) {
            IO.println("Chef", "Using " + ingredient + " raw for " + mealName);
        } else if (!needsCooking) {
            IO.println("Chef", ingredient + " doesn't need cooking for this dish");
        }

        // Notify dish preparer about this specific ingredient
        if (cutSuccess && cookSuccess) {
            String status = (needsCutting && shouldCook) ? "CUT_AND_COOKED"
                    : needsCutting ? "CUT" : shouldCook ? "COOKED" : "RAW";
            notifyDishPreparer(status, ingredient, mealName);
        }
    }

    private boolean cookIngredient(String ingredient) {
        if (!chefKnowledge.canCook(ingredient)) {
            IO.println("Chef", "ERROR - Don't know how to cook " + ingredient);
            IO.println("Chef", "Available ingredients for cooking: " + chefKnowledge.getCookableIngredients());
            return false;
        }

        Integer cookTime = chefKnowledge.getCookingTime(ingredient);
        String method = chefKnowledge.getCookingMethod(ingredient);

        IO.println("Chef", "Starting to cook " + ingredient + " using " + method +
                " (will take " + cookTime + " seconds)");

        // Simulate cooking time
        try {
            Thread.sleep(cookTime * 1000); // Convert to milliseconds
        } catch (InterruptedException e) {
            IO.println("Chef", "ERROR - Cooking interrupted for " + ingredient);
            Thread.currentThread().interrupt(); // Restore interrupted status
            return false;
        }

        IO.println("Chef", "SUCCESS - Finished cooking " + ingredient + " using " + method);
        return true;
    }

    private boolean cutIngredient(String ingredient) {
        if (!chefKnowledge.canCut(ingredient)) {
            IO.println("Chef", "ERROR - Don't know how to cut " + ingredient);
            IO.println("Chef", "Available ingredients for cutting: " + chefKnowledge.getCuttableIngredients());
            return false;
        }

        Integer cutTime = chefKnowledge.getCuttingTime(ingredient);

        IO.println("Chef", "Starting to cut " + ingredient + " (will take " + cutTime + " seconds)");

        // Simulate cutting time
        try {
            Thread.sleep(cutTime * 1000); // Convert to milliseconds
        } catch (InterruptedException e) {
            IO.println("Chef", "ERROR - Cutting interrupted for " + ingredient);
            Thread.currentThread().interrupt(); // Restore interrupted status
            return false;
        }

        IO.println("Chef", "SUCCESS - Finished cutting " + ingredient);
        return true;
    }

    private void notifyDishPreparer(String status, String ingredient, String mealName) {
        // Create message to notify dish preparer
        ACLMessage notification = new ACLMessage(ACLMessage.INFORM);

        // Set recipient (Dish Preparer agent)
        AID preparadorAID = new AID("dishPreparer", AID.ISLOCALNAME);
        notification.addReceiver(preparadorAID);

        // Set message content with ingredient status and meal name
        // With the format: "INGREDIENT_READY:STATUS:INGREDIENT:MEAL"
        // e.g. "INGREDIENT_READY:COOKED:meat:super_meat_boy"
        notification.setContent("INGREDIENT_READY:" + status + ":" + ingredient + ":" + mealName);

        // Send notification
        send(notification);

        IO.println("Chef", "Notified dish preparer that " + ingredient + " is "
                + status.toLowerCase().replace("_", " ") + " for meal " + mealName);
    }
}
