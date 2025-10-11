package underfried.ui;

import underfried.Restaurant;
import underfried.ui.AgentSprite.AgentType;
import underfried.ui.Station.StationType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Manages the game state and synchronizes with JADE agents
 */
public class GameState {
    private Restaurant restaurant;
    private List<AgentSprite> agents;
    private List<Station> stations;
    private List<GameObject> objects;
    private Map<String, AgentSprite> agentMap;

    // Environmental condition management
    private Random random;
    private long lastFireCheck;
    private long lastRatCheck;
    private static final long FIRE_CHECK_INTERVAL = 30000; // Check every 30 seconds
    private static final long RAT_CHECK_INTERVAL = 45000; // Check every 45 seconds
    private static final double FIRE_PROBABILITY = 0.15; // 15% chance
    private static final double RAT_PROBABILITY = 0.20; // 20% chance

    // Predefined positions for agents and stations
    private static final double CHEF_START_X = 2.5;
    private static final double CHEF_START_Y = 2.5;
    private static final double WAITER_START_X = 9.5;
    private static final double WAITER_START_Y = 7.0;
    private static final double DISH_PREPARER_START_X = 7.5;
    private static final double DISH_PREPARER_START_Y = 2.5;
    private static final double DISH_WASHER_START_X = 7.5;
    private static final double DISH_WASHER_START_Y = 11.5;

    public GameState(Restaurant restaurant) {
        this.restaurant = restaurant;
        this.agents = new ArrayList<>();
        this.stations = new ArrayList<>();
        this.objects = new ArrayList<>();
        this.agentMap = new HashMap<>();
        this.random = new Random();
        this.lastFireCheck = System.currentTimeMillis();
        this.lastRatCheck = System.currentTimeMillis();

        initializeStations();
        initializeAgents();
    }

    private void initializeStations() {
        // Kitchen stations
        stations.add(new Station("Cooking", StationType.COOKING_STATION, 1, 1, 2, 2));
        stations.add(new Station("Cutting", StationType.CUTTING_STATION, 4, 1, 2, 2));
        stations.add(new Station("Prep", StationType.PREP_STATION, 7, 1, 2, 2));
        stations.add(new Station("Washing", StationType.WASHING_STATION, 7, 10, 2, 2));

        // Pass-through counter
        stations.add(new Station("Counter", StationType.COUNTER, 9, 6, 2, 3));

        // Dining tables
        stations.add(new Station(null, StationType.TABLE, 12, 2, 1.5, 1.5));
        stations.add(new Station(null, StationType.TABLE, 15, 2, 1.5, 1.5));
        stations.add(new Station(null, StationType.TABLE, 18, 2, 1.5, 1.5));
        stations.add(new Station(null, StationType.TABLE, 12, 5, 1.5, 1.5));
        stations.add(new Station(null, StationType.TABLE, 15, 5, 1.5, 1.5));
        stations.add(new Station(null, StationType.TABLE, 18, 5, 1.5, 1.5));
        stations.add(new Station(null, StationType.TABLE, 12, 8, 1.5, 1.5));
        stations.add(new Station(null, StationType.TABLE, 15, 8, 1.5, 1.5));
        stations.add(new Station(null, StationType.TABLE, 18, 8, 1.5, 1.5));
        stations.add(new Station(null, StationType.TABLE, 12, 11, 1.5, 1.5));
        stations.add(new Station(null, StationType.TABLE, 15, 11, 1.5, 1.5));
        stations.add(new Station(null, StationType.TABLE, 18, 11, 1.5, 1.5));
    }

    private void initializeAgents() {
        // Create visual representations for each agent
        AgentSprite chef = new AgentSprite("Chef", AgentType.CHEF, CHEF_START_X, CHEF_START_Y);
        AgentSprite waiter = new AgentSprite("Waiter", AgentType.WAITER, WAITER_START_X, WAITER_START_Y);
        AgentSprite dishPreparer = new AgentSprite("Preparer", AgentType.DISH_PREPARER,
                DISH_PREPARER_START_X, DISH_PREPARER_START_Y);
        AgentSprite dishWasher = new AgentSprite("Washer", AgentType.DISH_WASHER,
                DISH_WASHER_START_X, DISH_WASHER_START_Y);

        agents.add(chef);
        agents.add(waiter);
        agents.add(dishPreparer);
        agents.add(dishWasher);

        agentMap.put("chef", chef);
        agentMap.put("waiter", waiter);
        agentMap.put("dishPreparer", dishPreparer);
        agentMap.put("dishWasher", dishWasher);
    }

    public void update() {
        // Update all agents
        for (AgentSprite agent : agents) {
            agent.update();
        }

        // Check for environmental conditions
        checkForEnvironmentalConditions();

        // Update condition game objects
        syncConditionObjects();
    }

    /**
     * Periodically check and spawn environmental conditions (fire, rats)
     */
    private void checkForEnvironmentalConditions() {
        long currentTime = System.currentTimeMillis();

        // Check for fire spawning
        if (currentTime - lastFireCheck > FIRE_CHECK_INTERVAL) {
            lastFireCheck = currentTime;
            if (random.nextDouble() < FIRE_PROBABILITY) {
                spawnFire();
            }
        }

        // Check for rat spawning
        if (currentTime - lastRatCheck > RAT_CHECK_INTERVAL) {
            lastRatCheck = currentTime;
            if (random.nextDouble() < RAT_PROBABILITY) {
                spawnRat();
            }
        }
    }

    /**
     * Spawn a fire at a random cooking station
     */
    private void spawnFire() {
        // Fire can spawn at cooking or cutting stations
        double[][] fireLocations = {
                { 2.0, 2.0 }, // Cooking station
                { 5.0, 2.0 } // Cutting station
        };

        int locationIndex = random.nextInt(fireLocations.length);
        double x = fireLocations[locationIndex][0];
        double y = fireLocations[locationIndex][1];

        restaurant.addCondition(Restaurant.EnvironmentalCondition.FIRE, x, y);
        System.out.println("[GameState] Fire started at location (" + x + ", " + y + ")!");
    }

    /**
     * Spawn a rat at a random location in the dining area
     */
    private void spawnRat() {
        // Rat can spawn anywhere in the dining area (x: 10-20, y: 0-14)
        double x = 10 + random.nextDouble() * 10;
        double y = random.nextDouble() * 14;

        restaurant.addCondition(Restaurant.EnvironmentalCondition.RAT, x, y);
        System.out.println("[GameState] Rat appeared at location (" + x + ", " + y + ")!");
    }

    /**
     * Synchronize GameObject list with active conditions from Restaurant
     */
    private void syncConditionObjects() {
        // Remove resolved condition objects
        objects.removeIf(obj -> {
            if (obj.getType() == GameObject.ObjectType.FIRE ||
                    obj.getType() == GameObject.ObjectType.RAT ||
                    obj.getType() == GameObject.ObjectType.BURNED_FOOD) {

                // Check if this condition is still active
                boolean stillActive = false;
                for (Restaurant.ActiveCondition condition : restaurant.activeConditions) {
                    if (!condition.resolved &&
                            Math.abs(obj.getX() - condition.x) < 0.1 &&
                            Math.abs(obj.getY() - condition.y) < 0.1) {
                        stillActive = true;
                        break;
                    }
                }
                return !stillActive;
            }
            return false;
        });

        // Add new condition objects
        for (Restaurant.ActiveCondition condition : restaurant.activeConditions) {
            if (!condition.resolved) {
                // Check if we already have a GameObject for this condition
                boolean hasObject = false;
                for (GameObject obj : objects) {
                    if (Math.abs(obj.getX() - condition.x) < 0.1 &&
                            Math.abs(obj.getY() - condition.y) < 0.1) {
                        hasObject = true;
                        break;
                    }
                }

                if (!hasObject) {
                    GameObject.ObjectType objectType = null;
                    String name = "";

                    switch (condition.type) {
                        case FIRE:
                            objectType = GameObject.ObjectType.FIRE;
                            name = "Fire";
                            break;
                        case RAT:
                            objectType = GameObject.ObjectType.RAT;
                            name = "Rat";
                            break;
                        case BURNED_FOOD:
                            objectType = GameObject.ObjectType.BURNED_FOOD;
                            name = "Burned " + (condition.affectedItem != null ? condition.affectedItem : "food");
                            break;
                    }

                    if (objectType != null) {
                        objects.add(new GameObject(name, objectType, condition.x, condition.y));
                    }
                }
            }
        }

        // Clean up resolved conditions from restaurant
        restaurant.cleanupResolvedConditions();
    }

    public void updateAgentStatus(String agentName, String status) {
        AgentSprite agent = agentMap.get(agentName);
        if (agent != null) {
            agent.setStatus(status);
        }
    }

    public void moveAgent(String agentName, double x, double y) {
        AgentSprite agent = agentMap.get(agentName);
        if (agent != null) {
            agent.setTargetPosition(x, y);
        }
    }

    // Getters
    public Restaurant getRestaurant() {
        return restaurant;
    }

    public List<AgentSprite> getAgents() {
        return agents;
    }

    public List<Station> getStations() {
        return stations;
    }

    public List<GameObject> getObjects() {
        return objects;
    }

    public AgentSprite getAgent(String name) {
        return agentMap.get(name);
    }

    /**
     * Add a burned food condition at a specific location
     * 
     * @param x        x position in tile coordinates
     * @param y        y position in tile coordinates
     * @param dishName the name of the burned dish
     */
    public void addBurnedFood(double x, double y, String dishName) {
        restaurant.addBurnedFood(x, y, dishName);
    }

    /**
     * Resolve a condition at a specific location
     * 
     * @param x x position in tile coordinates
     * @param y y position in tile coordinates
     */
    public void resolveConditionAt(double x, double y) {
        for (Restaurant.ActiveCondition condition : restaurant.activeConditions) {
            if (!condition.resolved &&
                    Math.abs(condition.x - x) < 1.5 &&
                    Math.abs(condition.y - y) < 1.5) {
                restaurant.resolveCondition(condition);
                System.out.println("[GameState] Resolved " + condition.type.getDisplayName() +
                        " at (" + x + ", " + y + ")");
            }
        }
    }
}
