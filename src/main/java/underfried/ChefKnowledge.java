package underfried;

import java.util.HashMap;
import java.util.Map;

/**
 * ChefKnowledge class that contains all the chef's expertise about
 * ingredient preparation, including cooking and cutting times.
 * This represents the chef's professional knowledge and skills.
 */
public class ChefKnowledge {
    private Map<String, Integer> cookingTimes;
    private Map<String, Integer> cuttingTimes;
    private Map<String, String> cookingMethods;
    private Map<String, IngredientProcessing> processingRequirements;
    private String chefName;

    /**
     * Enum to define what processing an ingredient requires
     */
    public enum IngredientProcessing {
        CUT_ONLY, // Only needs cutting (e.g., lettuce for salad)
        COOK_ONLY, // Only needs cooking (e.g., pasta)
        CUT_AND_COOK, // Needs both cutting and cooking (e.g., potato for stew)
        CUT_OR_COOK // Can be either cut (raw) or cooked depending on dish (e.g., tomato)
    }

    public ChefKnowledge(String chefName) {
        this.chefName = chefName;
        this.cookingTimes = new HashMap<>();
        this.cuttingTimes = new HashMap<>();
        this.cookingMethods = new HashMap<>();
        this.processingRequirements = new HashMap<>();
        initializeKnowledge();
    }

    /**
     * Initialize the chef's knowledge about ingredient preparation
     */
    private void initializeKnowledge() {
        // Cooking times (in seconds) - represents chef's experience
        cookingTimes.put("meat", 8);
        cookingTimes.put("chicken", 6);
        cookingTimes.put("fish", 4);
        cookingTimes.put("pasta", 5);
        cookingTimes.put("rice", 7);

        // Cutting times (in seconds) - represents chef's knife skills
        cuttingTimes.put("meat", 5);
        cuttingTimes.put("chicken", 5);
        cuttingTimes.put("fish", 15);
        cuttingTimes.put("tomato", 2);
        cuttingTimes.put("onion", 3);
        cuttingTimes.put("lettuce", 1);
        cuttingTimes.put("carrot", 3);
        cuttingTimes.put("potato", 4);

        // Cooking methods - represents chef's technique knowledge
        cookingMethods.put("meat", "grilling");
        cookingMethods.put("chicken", "pan-frying");
        cookingMethods.put("fish", "steaming");
        cookingMethods.put("pasta", "boiling");
        cookingMethods.put("rice", "steaming");

        // Processing requirements - what each ingredient actually needs
        processingRequirements.put("meat", IngredientProcessing.CUT_AND_COOK); // Meat must be cut and cooked
        processingRequirements.put("chicken", IngredientProcessing.CUT_AND_COOK); // Chicken must be cut and cooked
        processingRequirements.put("fish", IngredientProcessing.CUT_AND_COOK); // Fish must be cut and cooked
        processingRequirements.put("pasta", IngredientProcessing.COOK_ONLY); // Pasta only needs cooking
        processingRequirements.put("rice", IngredientProcessing.COOK_ONLY); // Rice only needs cooking

        processingRequirements.put("lettuce", IngredientProcessing.CUT_ONLY); // Lettuce is never cooked in these dishes
        processingRequirements.put("tomato", IngredientProcessing.CUT_OR_COOK); // Tomato can be raw or cooked
        processingRequirements.put("onion", IngredientProcessing.CUT_OR_COOK); // Onion can be raw or cooked
        processingRequirements.put("carrot", IngredientProcessing.CUT_AND_COOK); // Carrot usually needs both
        processingRequirements.put("potato", IngredientProcessing.CUT_AND_COOK); // Potato needs both cutting and
                                                                                 // cooking
    }

    /**
     * Get the cooking time for a specific ingredient
     * 
     * @param ingredient the ingredient to cook
     * @return cooking time in seconds, or null if ingredient is unknown
     */
    public Integer getCookingTime(String ingredient) {
        return cookingTimes.get(ingredient.toLowerCase());
    }

    /**
     * Get the cutting time for a specific ingredient
     * 
     * @param ingredient the ingredient to cut
     * @return cutting time in seconds, or null if ingredient is unknown
     */
    public Integer getCuttingTime(String ingredient) {
        return cuttingTimes.get(ingredient.toLowerCase());
    }

    /**
     * Get the cooking method for a specific ingredient
     * 
     * @param ingredient the ingredient
     * @return the preferred cooking method, or "unknown" if not specified
     */
    public String getCookingMethod(String ingredient) {
        return cookingMethods.getOrDefault(ingredient.toLowerCase(), "unknown");
    }

    /**
     * Check if the chef knows how to cook a specific ingredient
     * 
     * @param ingredient the ingredient to check
     * @return true if chef knows how to cook it, false otherwise
     */
    public boolean canCook(String ingredient) {
        return cookingTimes.containsKey(ingredient.toLowerCase());
    }

    /**
     * Check if the chef knows how to cut a specific ingredient
     * 
     * @param ingredient the ingredient to check
     * @return true if chef knows how to cut it, false otherwise
     */
    public boolean canCut(String ingredient) {
        return cuttingTimes.containsKey(ingredient.toLowerCase());
    }

    /**
     * Get the processing requirements for a specific ingredient
     * 
     * @param ingredient the ingredient to check
     * @return the processing requirement, or null if ingredient is unknown
     */
    public IngredientProcessing getProcessingRequirement(String ingredient) {
        return processingRequirements.get(ingredient.toLowerCase());
    }

    /**
     * Check if an ingredient needs cutting based on its processing requirements
     * 
     * @param ingredient the ingredient to check
     * @return true if the ingredient needs cutting
     */
    public boolean needsCutting(String ingredient) {
        IngredientProcessing requirement = getProcessingRequirement(ingredient);
        return requirement == IngredientProcessing.CUT_ONLY ||
                requirement == IngredientProcessing.CUT_AND_COOK ||
                requirement == IngredientProcessing.CUT_OR_COOK;
    }

    /**
     * Check if an ingredient needs cooking based on its processing requirements
     * 
     * @param ingredient the ingredient to check
     * @return true if the ingredient needs cooking
     */
    public boolean needsCooking(String ingredient) {
        IngredientProcessing requirement = getProcessingRequirement(ingredient);
        return requirement == IngredientProcessing.COOK_ONLY ||
                requirement == IngredientProcessing.CUT_AND_COOK ||
                requirement == IngredientProcessing.CUT_OR_COOK;
    }

    /**
     * Determine if an ingredient should be cooked or used raw based on the dish
     * context
     * For CUT_OR_COOK ingredients, this could be enhanced with dish-specific logic
     * 
     * @param ingredient the ingredient
     * @param dishName   the dish being prepared (for future context-aware
     *                   decisions)
     * @return true if should be cooked, false if should be used raw
     */
    public boolean shouldCookForDish(String ingredient, String dishName) {
        IngredientProcessing requirement = getProcessingRequirement(ingredient);

        if (requirement == IngredientProcessing.COOK_ONLY || requirement == IngredientProcessing.CUT_AND_COOK) {
            return true;
        }

        if (requirement == IngredientProcessing.CUT_ONLY) {
            return false;
        }

        // For CUT_OR_COOK ingredients, decide based on dish type
        if (requirement == IngredientProcessing.CUT_OR_COOK) {
            // Simple heuristic: salads get raw ingredients, other dishes get cooked
            if (dishName != null && dishName.toLowerCase().contains("salad")) {
                return false; // Use raw in salads
            }
            return true; // Cook in other dishes
        }

        return false; // Default to raw if unknown
    }

    /**
     * Get all ingredients the chef knows how to cook
     * 
     * @return set of cookable ingredients
     */
    public java.util.Set<String> getCookableIngredients() {
        return cookingTimes.keySet();
    }

    /**
     * Get all ingredients the chef knows how to cut
     * 
     * @return set of cuttable ingredients
     */
    public java.util.Set<String> getCuttableIngredients() {
        return cuttingTimes.keySet();
    }

    /**
     * Print all the chef's knowledge
     */
    public void printKnowledge() {
        System.out.println("=== Chef " + chefName + "'s Knowledge ===");
        System.out.println("Cooking expertise:");
        for (Map.Entry<String, Integer> entry : cookingTimes.entrySet()) {
            String method = cookingMethods.get(entry.getKey());
            System.out.println("  " + entry.getKey() + ": " + entry.getValue() + "s (" + method + ")");
        }
        System.out.println("Cutting expertise:");
        for (Map.Entry<String, Integer> entry : cuttingTimes.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue() + "s");
        }
    }
}