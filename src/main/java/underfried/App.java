package underfried;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class App {
    /**
     * Main method to launch JADE platform with it's agents
     */
    public static void main(String[] args) {
        Restaurant restaurant = new Restaurant("Underfried Restaurant");

        // Start JADE runtime
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.GUI, "true");
        AgentContainer ac = rt.createMainContainer(p);

        // Create agents with their necessary arguments
        try {
            Object[] agentArgs = new Object[] { restaurant };

            AgentController chef = ac.createNewAgent("chef", "underfried.agents.Chef", agentArgs);
            AgentController waiter = ac.createNewAgent("waiter", "underfried.agents.Waiter", agentArgs);
            AgentController dishPreparer = ac.createNewAgent("dishPreparer", "underfried.agents.DishPreparer",
                    agentArgs);
            AgentController dishWasher = ac.createNewAgent("dishWasher", "underfried.agents.DishWasher", agentArgs);

            // TODO: Current problems:
            // - The Chef may fail to process the orders. And does not have any mechanism
            // about these failures.
            // - There is no mechanism to map the orders to the corresponding dishes being
            // prepared.

            chef.start();
            waiter.start();
            dishPreparer.start();
            dishWasher.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
