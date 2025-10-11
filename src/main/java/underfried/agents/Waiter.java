package underfried.agents;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import underfried.Restaurant;
import underfried.ui.GameWindow;
import underfried.behaviors.ConditionalTickerBehavior;
import underfried.IO;

enum WaiterState {
    KITCHEN,
    DINING_AREA
}

public class Waiter extends Agent {
    private Restaurant restaurant = null;
    private GameWindow gameWindow = null;
    private WaiterState currentState = WaiterState.KITCHEN;

    private boolean isBusy = false;

    private int ordersTaken = 0;
    private int emptyPlatesTaken = 0;

    protected void setup() {
        Object[] args = getArguments();
        restaurant = (Restaurant) args[0];
        if (args.length > 1) {
            gameWindow = (GameWindow) args[1];
        }

        logToUI("Waiter ready to serve!");

        addBehaviour(new PeekDiningAreaBehavior(this, 10000));
        addBehaviour(new PeekReadyDishesBehavior(this, 5000));
    }

    /**
     * Check for rats and bonk them!
     */
    private void checkForRats() {
        if (restaurant.getConditionCount(Restaurant.EnvironmentalCondition.RAT) > 0) {
            java.util.List<Restaurant.ActiveCondition> rats = restaurant
                    .getConditionsByType(Restaurant.EnvironmentalCondition.RAT);

            for (Restaurant.ActiveCondition rat : rats) {
                if (!rat.resolved) {
                    IO.println(getAID().getName(), "RAT SPOTTED at (" + rat.x + ", " + rat.y + ")!");
                    logToUI("ALERT: Rat spotted by Waiter!");

                    if (gameWindow != null) {
                        gameWindow.getGameState().updateAgentStatus("waiter", "Chasing rat!");
                    }

                    // Move to rat location
                    IO.println(getAID().getName(), "Moving to catch the rat...");
                    if (gameWindow != null) {
                        gameWindow.getGameState().moveAgent("waiter", rat.x, rat.y);
                        gameWindow.waitUntilArrived("waiter", rat.x, rat.y);
                    }

                    // BONK the rat!
                    IO.println(getAID().getName(), "BONK! Got that rat!");
                    logToUI("Waiter bonked the rat!");

                    if (gameWindow != null) {
                        gameWindow.getGameState().updateAgentStatus("waiter", "Bonked rat!");
                    }

                    // Wait a moment for the bonk animation
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    // Alert other agents about successful rat elimination
                    ACLMessage alert = new ACLMessage(ACLMessage.INFORM);
                    alert.addReceiver(new AID("chef", AID.ISLOCALNAME));
                    alert.addReceiver(new AID("dishWasher", AID.ISLOCALNAME));
                    alert.setContent("RAT_ELIMINATED:" + rat.x + "," + rat.y);
                    send(alert);

                    IO.println(getAID().getName(), "Alerted other agents - rat has been eliminated!");

                    // Mark as resolved (rat was bonked and eliminated)
                    restaurant.resolveCondition(rat);
                    IO.println(getAID().getName(), "Rat eliminated successfully!");
                    logToUI("Rat eliminated by Waiter!");
                }
            }
        }
    }

    private class PeekDiningAreaBehavior extends ConditionalTickerBehavior {
        public PeekDiningAreaBehavior(Agent a, long timeout) {
            super(a, timeout);
        }

        protected boolean testCondition() {
            return !isBusy;
        }

        protected void execute() {
            IO.println(getAID().getName() + ": I'll take a look at the tables.");

            isBusy = true;

            // Check for rats first
            checkForRats();

            takeOrders();
            takeEmptyPlates();

            goTo(WaiterState.KITCHEN);
            IO.println(getAID().getName() + ": I'm back with " + ordersTaken + " orders and " + emptyPlatesTaken
                    + " empty plates.");

            restaurant.dirtyPlates += emptyPlatesTaken;

            if (ordersTaken > 0) {
                String[] availableDishes = restaurant.getAvailableDishes().toArray(new String[0]);
                String ordersMessage = "";

                for (int i = 0; i < ordersTaken; i++) {
                    int dishIndex = (int) (Math.random() * availableDishes.length);
                    String dishOrdered = availableDishes[dishIndex];

                    // Add order to shared state for validation/control
                    if (restaurant.addOrder(dishOrdered)) {
                        ordersMessage += dishOrdered + "\n";
                        IO.println(getAID().getName() + ": Added order for " + dishOrdered + " to tracking queue.");
                        logToUI("New order: " + dishOrdered);
                    } else {
                        IO.println(getAID().getName() + ": ERROR - Unknown dish: " + dishOrdered);
                    }
                }

                // Send ACL message to Chef with all orders
                if (!ordersMessage.isEmpty()) {
                    ACLMessage orderMessage = new ACLMessage(ACLMessage.REQUEST);
                    AID chefAID = new AID("chef", AID.ISLOCALNAME);
                    orderMessage.addReceiver(chefAID);
                    orderMessage.setContent(ordersMessage.trim());
                    send(orderMessage);

                    if (gameWindow != null) {
                        gameWindow.getGameState().updateAgentStatus("waiter", "Sent " + ordersTaken + " orders");
                    }

                    IO.println(getAID().getName() + ": Sent " + ordersTaken + " order(s) to Chef via message.");
                    IO.println(getAID().getName() + ": [VALIDATION] Total orders in tracking queue: " +
                            restaurant.getPendingOrderCount());
                }

                ordersTaken = 0;
            }

            // Notify the dishwasher about dirty plates
            if (emptyPlatesTaken > 0) {
                ACLMessage dirtyPlatesNotification = new ACLMessage(ACLMessage.INFORM);
                AID dishWasherAID = new AID("dishWasher", AID.ISLOCALNAME);
                dirtyPlatesNotification.addReceiver(dishWasherAID);
                dirtyPlatesNotification.setContent("DIRTY_PLATES:" + emptyPlatesTaken);
                send(dirtyPlatesNotification);

                IO.println(
                        getAID().getName() + ": Notified dishwasher about " + emptyPlatesTaken + " dirty plates.");
                emptyPlatesTaken = 0;
            }

            isBusy = false;
        }
    }

    private class PeekReadyDishesBehavior extends ConditionalTickerBehavior {
        public PeekReadyDishesBehavior(Agent a, long timeout) {
            super(a, timeout);
        }

        protected boolean testCondition() {
            return !isBusy;
        }

        protected void execute() {
            goTo(WaiterState.KITCHEN);

            // Validate shared state before picking up dishes
            int availableDishes = restaurant.readyDishes.size();
            IO.println(
                    getAID().getName() + ": [VALIDATION] Checking kitchen - Ready dishes: " + availableDishes);

            int mealsToTake = Math.min(2, availableDishes);

            if (mealsToTake > 0) {
                IO.println(getAID().getName() + ": [VALIDATION] ✓ Picking up " + mealsToTake + " dish(es)");
                List<String> mealsToDeliver = new ArrayList<>();

                for (int i = 0; i < mealsToTake; i++) {
                    String doneDish = restaurant.readyDishes.poll();
                    mealsToDeliver.add(doneDish);
                    IO.println(getAID().getName() + ": I've picked up the dish " + doneDish + " from the kitchen.");
                }

                IO.println(getAID().getName() + ": [VALIDATION] Remaining ready dishes: " +
                        restaurant.readyDishes.size());

                deliverMeals(mealsToDeliver);
            }
        }
    }

    protected void goTo(WaiterState destination) {
        if (currentState == destination)
            return;

        double targetX = 0, targetY = 0;
        // Update UI with movement BEFORE changing state
        if (gameWindow != null) {
            if (destination == WaiterState.KITCHEN) {
                targetX = 9.5;
                targetY = 7.0;
                gameWindow.getGameState().updateAgentStatus("waiter", "Going to kitchen");
            } else {
                // Move to center of dining area
                targetX = 15.0;
                targetY = 7.0;
                gameWindow.getGameState().updateAgentStatus("waiter", "Going to dining area");
            }

            gameWindow.getGameState().moveAgent("waiter", targetX, targetY);
            gameWindow.waitUntilArrived("waiter", targetX, targetY);
        }

        currentState = destination;
    }

    protected void takeOrders() {
        goTo(WaiterState.DINING_AREA);

        if (gameWindow != null) {
            gameWindow.getGameState().updateAgentStatus("waiter", "Taking orders");
        }

        for (int i = 0; i < 3; i++) {
            if (Math.random() < 0.3) {
                gameWindow.wait(1000);
                ordersTaken++;
                IO.println(getAID().getName() + ": I got an order.");
            }
        }
    }

    protected void takeEmptyPlates() {
        goTo(WaiterState.DINING_AREA);

        // Try to take up to 5 empty plates, but only if they're actually available
        int attemptsMade = 0;
        int maxAttempts = 5;

        while (attemptsMade < maxAttempts && restaurant.takenPlates > 0) {
            if (Math.random() < 0.3) {
                gameWindow.wait(1000);
                IO.println(getAID().getName() + ": I took an empty plate.");
                emptyPlatesTaken++;
                restaurant.takenPlates--; // Decrement taken plates immediately
            }
            attemptsMade++;
        }
    }

    protected void deliverMeals(List<String> mealsToDeliver) {
        goTo(WaiterState.DINING_AREA);

        if (gameWindow != null) {
            gameWindow.getGameState().updateAgentStatus("waiter", "Delivering meals");
        }

        for (String meal : mealsToDeliver) {
            gameWindow.wait(1000);
            IO.println(getAID().getName() + ": Delivering the dish " + meal + " to a table.");
            logToUI("Delivered " + meal + " to table");
            // When a meal is delivered, the customer now has a plate
            restaurant.takenPlates++;
        }
    }

    private void logToUI(String message) {
        if (gameWindow != null) {
            gameWindow.appendLog("[Waiter] " + message);
        }
    }
}
