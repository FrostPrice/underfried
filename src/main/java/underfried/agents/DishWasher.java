package underfried.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import underfried.Restaurant;
import underfried.ui.GameWindow;
import underfried.IO;

enum DishWasherState {
    WASHING_STATION,
    DELIVERING_CLEAN_PLATES,
    TAKING_DIRTY_PLATES
}

public class DishWasher extends Agent {
    private Restaurant restaurant;
    private GameWindow gameWindow;
    private int washingCapacity = 5; // Maximum plates that can be washed at once
    private int washingTimePerPlate = 2000; // 2 seconds per plate in milliseconds

    private DishWasherState currentState = DishWasherState.WASHING_STATION;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            restaurant = (Restaurant) args[0];
            if (args.length > 1) {
                gameWindow = (GameWindow) args[1];
            }
        } else {
            throw new IllegalArgumentException("DishWasher agent missing required arguments: Restaurant instance");
        }

        IO.println("DishWasher", "Agent " + getAID().getName() + " is ready to wash dishes!");
        IO.println("DishWasher", "Washing capacity: " + washingCapacity + " plates at once");
        IO.println("DishWasher", "Washing time: " + (washingTimePerPlate / 1000) + " seconds per plate");
        IO.println("DishWasher", "Current dirty plates in restaurant: " + restaurant.dirtyPlates);
        logToUI("DishWasher ready to clean plates!");

        addBehaviour(new DishWashingBehaviour());
    }

    private void logToUI(String message) {
        if (gameWindow != null) {
            gameWindow.appendLog("[DishWasher] " + message);
        }
    }

    @Override
    protected void takeDown() {
        IO.println("DishWasher", "Agent " + getAID().getName() + " is finishing work.");
        IO.println("DishWasher", "Final stats - Dirty plates remaining: " + restaurant.dirtyPlates +
                ", Clean plates available: " + restaurant.cleanPlates);
    }

    private class DishWashingBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            // Check for incoming messages first
            ACLMessage msg = receive();
            if (msg != null) {
                String content = msg.getContent();
                IO.println("DishWasher", "Received message: " + content);

                if (content != null && !content.trim().isEmpty()) {
                    processMessage(content, msg.getSender());
                } else {
                    IO.println("DishWasher", "Received empty message from " + msg.getSender().getName());
                }
            }

            // Check if there are dirty plates to wash
            if (restaurant.dirtyPlates > 0) {
                washDirtyPlates();
            } else {
                if (msg == null) {
                    block();
                }
            }
        }
    }

    private void processMessage(String content, AID sender) {
        try {
            if (content.startsWith("DIRTY_PLATES:")) {
                goTo(DishWasherState.TAKING_DIRTY_PLATES);

                gameWindow.wait(500);
                goTo(DishWasherState.WASHING_STATION);

                handleDirtyPlatesNotification(content, sender);
            } else {
                IO.println("DishWasher", "Unknown message format: " + content);
            }
        } catch (Exception e) {
            IO.println("DishWasher", "ERROR - Exception processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDirtyPlatesNotification(String content, AID sender) {
        // Parse message format: "DIRTY_PLATES:COUNT"
        // e.g. "DIRTY_PLATES:3"
        String[] parts = content.split(":");
        if (parts.length != 2) {
            IO.println("DishWasher", "ERROR - Invalid dirty plates format: " + content);
            return;
        }

        try {
            int plateCount = Integer.parseInt(parts[1]);
            IO.println("DishWasher", "Received notification of " + plateCount + " dirty plates from " +
                    sender.getName());
            IO.println("DishWasher", "Total dirty plates now available: " + restaurant.dirtyPlates);

            // The dirty plates are already added to restaurant.dirtyPlates by the waiter
            // We just acknowledge the notification
            if (plateCount > 0) {
                IO.println("DishWasher", "Will start washing dishes now!");
            }
        } catch (NumberFormatException e) {
            IO.println("DishWasher", "ERROR - Invalid plate count: " + parts[1]);
        }
    }

    private void washDirtyPlates() {
        if (restaurant.dirtyPlates <= 0) {
            return;
        }

        // Determine how many plates to wash in this batch
        // Use Math.min to ensure we never try to wash more plates than are available
        int platesToWash = Math.min(restaurant.dirtyPlates, washingCapacity);

        // Double-check that we have plates to wash (guard against race conditions)
        if (platesToWash <= 0) {
            IO.println("DishWasher", "No plates to wash (race condition prevented)");
            return;
        }

        IO.println("DishWasher", "Starting to wash " + platesToWash + " dirty plates");
        IO.println("DishWasher", "Dirty plates available: " + restaurant.dirtyPlates);
        logToUI("Washing " + platesToWash + " dirty plates...");

        if (gameWindow != null) {
            gameWindow.getGameState().updateAgentStatus("dishWasher", "Washing");
        }

        // Remove dirty plates from the global count immediately to prevent other agents
        // from accessing them
        restaurant.dirtyPlates -= platesToWash;

        int totalWashTime = platesToWash * washingTimePerPlate;

        IO.println("DishWasher", "Washing " + platesToWash + " plates will take " +
                (totalWashTime / 1000) + " seconds");

        // Simulate washing time
        try {
            Thread.sleep(totalWashTime);
        } catch (InterruptedException e) {
            IO.println("DishWasher", "ERROR - Washing interrupted");
            Thread.currentThread().interrupt();
            // Return dirty plates to the count if washing was interrupted
            restaurant.dirtyPlates += platesToWash;
            return;
        }

        // Washing completed successfully
        IO.println("DishWasher", "SUCCESS - Finished washing " + platesToWash + " plates");
        IO.println("DishWasher", "Remaining dirty plates: " + restaurant.dirtyPlates);
        logToUI("Cleaned " + platesToWash + " plates!");

        // Send clean plates to DishPreparer
        sendCleanPlatesToDishPreparer(platesToWash);
    }

    private void sendCleanPlatesToDishPreparer(int cleanPlateCount) {
        // Create message to notify dish preparer about clean plates
        ACLMessage notification = new ACLMessage(ACLMessage.INFORM);
        goTo(DishWasherState.DELIVERING_CLEAN_PLATES);

        gameWindow.wait(500);
        goTo(DishWasherState.WASHING_STATION);

        // Set recipient (Dish Preparer agent)
        AID dishPreparerAID = new AID("dishPreparer", AID.ISLOCALNAME);
        notification.addReceiver(dishPreparerAID);

        // Set message content with clean plates count
        // Format: "CLEAN_PLATES:COUNT"
        // e.g. "CLEAN_PLATES:5"
        notification.setContent("CLEAN_PLATES:" + cleanPlateCount);

        // Send notification
        send(notification);

        IO.println("DishWasher", "Sent " + cleanPlateCount + " clean plates to DishPreparer");
        IO.println("DishWasher", "DishPreparer will update the restaurant's clean plate count");
    }

    protected void goTo(DishWasherState destination) {
        if (currentState == destination)
            return;

        double targetX = 0, targetY = 0;

        // Update UI with movement BEFORE changing state
        if (gameWindow != null) {
            switch (destination) {
                case WASHING_STATION:
                    targetX = 7.5;
                    targetY = 11.5;
                    gameWindow.getGameState().updateAgentStatus("dishWasher", "Going to washing station");
                    break;
                case DELIVERING_CLEAN_PLATES:
                    targetX = 7.5;
                    targetY = 3;
                    gameWindow.getGameState().updateAgentStatus("dishWasher", "Delivering clean plates to preparer");
                    break;
                case TAKING_DIRTY_PLATES:
                    targetX = 8;
                    targetY = 8;
                    gameWindow.getGameState().updateAgentStatus("dishWasher", "Taking dirty plates");
                    break;
            }

            gameWindow.getGameState().moveAgent("dishWasher", targetX, targetY);

            // Wait until agent has arrived at destination
            gameWindow.waitUntilArrived("dishWasher", targetX, targetY);
            gameWindow.getGameState().updateAgentStatus("dishWasher", null);

        }

        // Update state AFTER arriving at destination
        currentState = destination;
    }
}
