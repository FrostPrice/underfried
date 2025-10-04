package underfried.agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import underfried.Restaurant;

public class Waiter extends Agent {
    private Restaurant restaurant = null;
    private int ordersTaken = 0;
    private int emptyPlatesTaken = 0;

    protected void setup() {
        restaurant = (Restaurant) getArguments()[0];

        addBehaviour(new TickerBehaviour(this, 10000) {
            public void onTick() {
                IO.println(getAID().getName() + ": I'll take a look at the tables.");

                takeOrders();
                takeEmptyPlates();
                Restaurant.dirtyPlates += emptyPlatesTaken;
                emptyPlatesTaken = 0;

                IO.println(getAID().getName() + ": I'm back with " + ordersTaken + " orders and " + emptyPlatesTaken
                        + " empty plates.");

                restaurant.dirtyPlates += emptyPlatesTaken;
                emptyPlatesTaken = 0;

                // TODO Notify the chef

                // TODO Notify the dishwasher
            }
        });

    private void wait(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
        }
    }
    }

    protected void takeOrders() {
        for (int i = 0; i < 3; i++) {
            if (Math.random() < 0.3) {
                IO.println("I got an order.");
            }
        }
    }

    protected void takeEmptyPlates() {
        for (int i = 0; i < Math.min(restaurant.takenPlates, 5); i++) {
            if (Math.random() < 0.3) {
                emptyPlatesTaken++;
                IO.println("I took an empty plate.");
            }
        }
    }
}
