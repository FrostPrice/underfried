package underfried;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import underfried.ui.GameWindow;
import javax.swing.SwingUtilities;

public class App {
    private static GameWindow gameWindow;

    /**
     * Main method to launch JADE platform with it's agents and UI
     */
    public static void main(String[] args) {
        Restaurant restaurant = new Restaurant("Underfried Restaurant");

        // Initialize the game UI on the Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            gameWindow = new GameWindow(restaurant);
            gameWindow.appendLog("Restaurant opened!");
            gameWindow.appendLog("Initializing JADE agents...");
        });

        // Start JADE runtime
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.GUI, "true");
        AgentContainer ac = rt.createMainContainer(p);

        // Create agents with their necessary arguments
        try {
            Object[] agentArgs = new Object[] { restaurant, gameWindow };

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

            SwingUtilities.invokeLater(() -> {
                gameWindow.appendLog("All agents started successfully!");
                gameWindow.appendLog("Chef agent is ready to cook.");
                gameWindow.appendLog("Waiter agent is ready to serve.");
                gameWindow.appendLog("DishPreparer agent is ready to assemble dishes.");
                gameWindow.appendLog("DishWasher agent is ready to clean plates.");
                gameWindow.appendLog("\n--- Simulation Running ---\n");
            });

        } catch (Exception e) {
            e.printStackTrace();
            if (gameWindow != null) {
                SwingUtilities.invokeLater(() -> {
                    gameWindow.appendLog("ERROR: Failed to start agents - " + e.getMessage());
                });
            }
        }

        // Add shutdown hook to clean up UI
        java.lang.Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (gameWindow != null) {
                gameWindow.cleanup();
            }
        }));
    }

    public static GameWindow getGameWindow() {
        return gameWindow;
    }
}
