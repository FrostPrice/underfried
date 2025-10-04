package underfried;

public class App {
    /**
     * Main method to launch JADE platform with it's agents
     */
    public static void main(String[] args) {
        // Launch JADE with GUI and it's agents
        String[] jadeArgs = {
                "-gui",
                "-agents", "testAgent:underfried.TestAgent"
        };

        // Start JADE platform
        jade.Boot.main(jadeArgs);
    }
}
