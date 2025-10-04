package underfried.agents;

import java.util.ArrayList;
import java.util.List;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import underfried.Restaurant;

enum WaiterState {
    KITCHEN, DINING_AREA
}

public class Waiter extends Agent {
    private Restaurant restaurant = null;
    private WaiterState currentState = WaiterState.KITCHEN;

    private int ordersTaken = 0;
    private int emptyPlatesTaken = 0;

    private List<String> mealsToDeliver = new ArrayList<>();

    protected void setup() {
        restaurant = (Restaurant) getArguments()[0];

        addBehaviour(new TickerBehaviour(this, 10000) {
            public void onTick() {
                IO.println(getAID().getName() + ": I'll take a look at the tables.");

                takeOrders();
                takeEmptyPlates();

                goTo(WaiterState.KITCHEN);
                IO.println(getAID().getName() + ": I'm back with " + ordersTaken + " orders and " + emptyPlatesTaken
                        + " empty plates.");

                restaurant.dirtyPlates += emptyPlatesTaken;
                emptyPlatesTaken = 0;

                // TODO Notify the chef

                // TODO Notify the dishwasher
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
        }
        mealsToDeliver.clear();
    }
}
