package underfried.ui;

import underfried.Restaurant;
import underfried.ui.AgentSprite.AgentType;
import underfried.ui.Station.StationType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the game state and synchronizes with JADE agents
 */
public class GameState {
    private Restaurant restaurant;
    private List<AgentSprite> agents;
    private List<Station> stations;
    private List<GameObject> objects;
    private Map<String, AgentSprite> agentMap;

    // Predefined positions for agents and stations
    private static final double CHEF_START_X = 2.5;
    private static final double CHEF_START_Y = 2.5;
    private static final double WAITER_START_X = 9.5;
    private static final double WAITER_START_Y = 7.0;
    private static final double DISH_PREPARER_START_X = 7.5;
    private static final double DISH_PREPARER_START_Y = 2.5;
    private static final double DISH_WASHER_START_X = 7.5;
    private static final double DISH_WASHER_START_Y = 11.5;

    private int frameCount = 0;

    public GameState(Restaurant restaurant) {
        this.restaurant = restaurant;
        this.agents = new ArrayList<>();
        this.stations = new ArrayList<>();
        this.objects = new ArrayList<>();
        this.agentMap = new HashMap<>();

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
        frameCount++;

        // Update all agents
        for (AgentSprite agent : agents) {
            agent.update();
        }
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
}
