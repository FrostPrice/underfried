package underfried.agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import underfried.Restaurant;

public class Waiter extends Agent {
    private int ordersTaken = 0;
    private int emptyPlatesTaken = 0;

    protected void setup() {
        addBehaviour(new TickerBehaviour(this, 4000) {

            public void onTick() {
                IO.println(myAgent.getName() + ": I'll take a look at the tables.");
                takeOrders();
                takeEmptyPlates();
                Restaurant.dirtyPlates += emptyPlatesTaken;
                emptyPlatesTaken = 0;

                IO.println(myAgent.getName() + ": I'm back with " + ordersTaken + " orders and " + emptyPlatesTaken
                        + " empty plates.");

                // TODO Notify the chef

                // TODO Notify the dishwasher
            }
        });
    }

    protected void takeOrders() {
        for (int i = 0; i < 3; i++) {
            if (Math.random() < 0.3) {
                IO.println("I got an order.");
            }
        }
    }

    protected void takeEmptyPlates() {
        for (int i = 0; i < Math.min(Restaurant.takenPlates, 5); i++) {
            if (Math.random() < 0.3) {
                emptyPlatesTaken++;
                IO.println("I took an empty plate.");
            }
        }
    }
}
