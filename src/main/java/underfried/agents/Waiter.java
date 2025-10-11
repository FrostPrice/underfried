package underfried.agents;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import underfried.Restaurant;
import underfried.ui.GameWindow;

enum WaiterState {
    KITCHEN, DINING_AREA
}

public class Waiter extends Agent {
    private Restaurant restaurant = null;
    private GameWindow gameWindow = null;
    private WaiterState currentState = WaiterState.KITCHEN;

    private int ordersTaken = 0;
    private int emptyPlatesTaken = 0;

    private List<String> mealsToDeliver = new ArrayList<>();

    protected void setup() {
        Object[] args = getArguments();
        restaurant = (Restaurant) args[0];
        if (args.length > 1) {
            gameWindow = (GameWindow) args[1];
        }

        logToUI("Waiter ready to serve!");

        addBehaviour(new TickerBehaviour(this, 3000) {
            public void onTick() {
                IO.println(getAID().getName() + ": I'll take a look at the tables.");

                takeOrders();
                takeEmptyPlates();

                goTo(WaiterState.KITCHEN);
                IO.println(getAID().getName() + ": I'm back with " + ordersTaken + " orders and " + emptyPlatesTaken
                        + " empty plates.");

                restaurant.dirtyPlates += emptyPlatesTaken;

                // Process orders: Send to Chef via ACL AND update shared state for validation
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
                } // Notify the dishwasher about dirty plates
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
            }
        });

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                if (currentState == WaiterState.KITCHEN) {
                    // Validate shared state before picking up dishes
                    int availableDishes = restaurant.readyDishes.size();
                    IO.println(
                            getAID().getName() + ": [VALIDATION] Checking kitchen - Ready dishes: " + availableDishes);

                    int mealsToTake = Math.min(2, availableDishes);

                    if (mealsToTake > 0) {
                        IO.println(getAID().getName() + ": [VALIDATION] ✓ Picking up " + mealsToTake + " dish(es)");

                        for (int i = 0; i < mealsToTake; i++) {
                            String doneDish = restaurant.readyDishes.poll();
                            mealsToDeliver.add(doneDish);
                            IO.println(getAID().getName() + ": I've picked up the dish " + doneDish
                                    + " from the kitchen.");
                        }

                        IO.println(getAID().getName() + ": [VALIDATION] Remaining ready dishes: " +
                                restaurant.readyDishes.size());
                    }

                    deliverMeals();

                    goTo(WaiterState.KITCHEN);
                } else {
                    block();
                }
            }
        });
    }

    private void wait(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
        }
    }

    protected void goTo(WaiterState destination) {
        wait(3000);
        currentState = destination;
    }

    protected void takeOrders() {
        if (currentState == WaiterState.KITCHEN)
            goTo(WaiterState.DINING_AREA);

        for (int i = 0; i < 3; i++) {
            if (Math.random() < 0.3) {
                wait(1000);
                ordersTaken++;
                IO.println(getAID().getName() + ": I got an order.");
            }
        }
    }

    protected void takeEmptyPlates() {
        if (currentState == WaiterState.KITCHEN)
            goTo(WaiterState.DINING_AREA);

        for (int i = 0; i < Math.min(restaurant.takenPlates, 5); i++) {
            if (Math.random() < 0.3) {
                wait(1000);
                IO.println(getAID().getName() + ": I took an empty plate.");
                emptyPlatesTaken++;
            }
        }
    }

    protected void deliverMeals() {
        if (currentState == WaiterState.KITCHEN)
            goTo(WaiterState.DINING_AREA);

        for (String meal : mealsToDeliver) {
            wait(1000);
            IO.println(getAID().getName() + ": Delivering the dish " + meal + " to a table.");
            logToUI("Delivered " + meal + " to table");
        }
        mealsToDeliver.clear();
    }

    private void logToUI(String message) {
        if (gameWindow != null) {
            gameWindow.appendLog("[Waiter] " + message);
        }
    }
}
