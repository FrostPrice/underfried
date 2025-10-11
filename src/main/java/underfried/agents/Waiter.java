package underfried.agents;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import underfried.Restaurant;

enum WaiterState {
    KITCHEN,
    DINING_AREA
}

public class Waiter extends Agent {
    private Restaurant restaurant = null;
    private WaiterState currentState = WaiterState.KITCHEN;

    private int ordersTaken = 0;
    private int emptyPlatesTaken = 0;

    private List<String> mealsToDeliver = new ArrayList<>();

    protected void setup() {
        restaurant = (Restaurant) getArguments()[0];

        addBehaviour(new TickerBehaviour(this, 3000) {
            public void onTick() {
                IO.println(getAID().getName() + ": I'll take a look at the tables.");

                takeOrders();
                takeEmptyPlates();

                goTo(WaiterState.KITCHEN);
                IO.println(getAID().getName() + ": I'm back with " + ordersTaken + " orders and " + emptyPlatesTaken
                        + " empty plates.");

                restaurant.dirtyPlates += emptyPlatesTaken;

                if (ordersTaken > 0) {
                    String orders = "";
                    String[] availableDishes = restaurant.getAvailableDishes().toArray(new String[0]);

                    for (int i = 0; i < ordersTaken; i++) {
                        int dishIndex = (int) (Math.random() * availableDishes.length);
                        String dishOrdered = availableDishes[dishIndex];
                        orders += dishOrdered + "\n";
                        IO.println(getAID().getName() + ": Order of a " + dishOrdered + " to the chef.");
                    }

                    ACLMessage notification = new ACLMessage(ACLMessage.INFORM);
                    AID chefAID = new AID("chef", AID.ISLOCALNAME);
                    notification.addReceiver(chefAID);

                    notification.setContent(orders.trim());
                    send(notification);

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
            }
        });

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                if (currentState == WaiterState.KITCHEN) {
                    int mealsToTake = Math.min(2, restaurant.readyDishes.size());

                    for (int i = 0; i < mealsToTake; i++) {
                        String doneDish = restaurant.readyDishes.poll();
                        mealsToDeliver.add(doneDish);
                        IO.println(getAID().getName() + ": I've picked up the dish " + doneDish + " from the kitchen.");
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
        if (currentState == destination)
            return;
        wait(3000);
        currentState = destination;
    }

    protected void takeOrders() {
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
        goTo(WaiterState.DINING_AREA);

        for (int i = 0; i < Math.min(restaurant.takenPlates, 5); i++) {
            if (Math.random() < 0.3) {
                wait(1000);
                IO.println(getAID().getName() + ": I took an empty plate.");
                emptyPlatesTaken++;
            }
        }
    }

        goTo(WaiterState.DINING_AREA);

        for (String meal : mealsToDeliver) {
            wait(1000);
            IO.println(getAID().getName() + ": Delivering the dish " + meal + " to a table.");
        }
        mealsToDeliver.clear();
    }
}
