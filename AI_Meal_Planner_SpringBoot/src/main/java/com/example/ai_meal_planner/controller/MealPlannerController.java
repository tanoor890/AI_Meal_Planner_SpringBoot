package com.example.ai_meal_planner.controller;
import java.nio.charset.StandardCharsets;
import com.example.ai_meal_planner.model.Meal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.annotation.PostConstruct;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Controller
public class MealPlannerController {

    private Map<String, Meal> meals = new HashMap<>();

    @PostConstruct
    public void init() {
        initializeMeals();
    }

    @GetMapping("/")
    public String showForm(Model model) {
        return "index";
    }

    @PostMapping("/calculate")
    public String handleCalculation(@RequestParam Map<String, String> params, Model model) {
        String goal = params.get("goal");
        String ageStr = params.get("age");
        String weightStr = params.get("weight");
        String heightStr = params.get("height");
        String gender = params.get("gender");
        String activity = params.get("activity");
        String restrictions = params.get("restrictions");
        String allergies = params.get("allergies");

        double age = Double.parseDouble(ageStr);
        double weight = Double.parseDouble(weightStr);
        double height = Double.parseDouble(heightStr);

        double bmr = calculateBMR(gender, weight, height, age);
        double tdee = calculateTDEE(bmr, activity);

        List<Meal> mealPlan = generateMealPlan(goal, restrictions, allergies);

        List<String> groceryList = generateGroceryList(mealPlan);

        String mealPlanDescription = getMealPlanDescription(mealPlan, gender, weight, height, age, activity, goal);

        model.addAttribute("bmr", bmr);
        model.addAttribute("tdee", tdee);
        model.addAttribute("mealPlan", mealPlan);
        model.addAttribute("groceryList", groceryList);
        model.addAttribute("mealPlanDescription", mealPlanDescription);

        return "index";
    }

    @PostMapping("/feedback")
    public String handleFeedback(@RequestParam Map<String, String> params, Model model) {
        String feedback = params.get("feedback");
        model.addAttribute("message", "Thank you for your feedback!");
        return "index";
    }


private void initializeMeals() {
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("meals.json")) {
        if (inputStream != null) {
            String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(json);
            JSONArray mealArray = jsonObject.getJSONArray("meals");

            for (int i = 0; i < mealArray.length(); i++) {
                JSONObject mealJson = mealArray.getJSONObject(i);
                String name = mealJson.getString("name");
                JSONArray ingredientsJson = mealJson.getJSONArray("ingredients");
                List<String> ingredients = new ArrayList<>();
                for (int j = 0; j < ingredientsJson.length(); j++) {
                    ingredients.add(ingredientsJson.getString(j));
                }
                String recipe = mealJson.getString("recipe");

                meals.put(name, new Meal(name, ingredients, recipe));
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}


 private double calculateBMR(String gender, double weight, double height, double age) {
        if ("male".equalsIgnoreCase(gender)) {
            return (10 * weight) + (6.25 * height) - (5 * age) + 5;
        } else {
            return (10 * weight) + (6.25 * height) - (5 * age) - 161;
        }
    }

    private double calculateTDEE(double bmr, String activity) {
        double activityFactor = 1.2; 
        
        switch (activity) {
            case "sedentary":
                activityFactor = 1.2;
                break;
            case "lightly_active":
                activityFactor = 1.375;
                break;
            case "moderately_active":
                activityFactor = 1.55;
                break;
            case "very_active":
                activityFactor = 1.725;
                break;
            case "super_active":
                activityFactor = 1.9;
                break;
            default:
                activityFactor = 1.2;
                break;
        }
        return bmr * activityFactor;
    }

    private List<Meal> generateMealPlan(String goal, String restrictions, String allergies) {
        List<Meal> mealPlan = new ArrayList<>();

        
        List<String> restrictionList = Arrays.asList(restrictions.toLowerCase().split(","));
        List<String> allergyList = Arrays.asList(allergies.toLowerCase().split(","));

        for (Meal meal : meals.values()) {
            if (matchesGoal(meal, goal) && !containsRestrictions(meal, restrictionList) && !containsAllergens(meal, allergyList)) {
                mealPlan.add(meal);
            }
        }
          Collections.shuffle(mealPlan);
        return mealPlan.subList(0, Math.min(3, mealPlan.size()));
    }

    private boolean matchesGoal(Meal meal, String goal) {
    switch (goal.toLowerCase()) {
        case "Weight Gain":
            return meal.getIngredients().contains("chicken") || meal.getIngredients().contains("fish") || meal.getIngredients().contains("tofu");
        case "Weight Loss":
            return !meal.getIngredients().contains("butter") && !meal.getIngredients().contains("cream");
        case "Maintenance":
            return true;
        default:
            return true;
    }
}
    


    private boolean containsRestrictions(Meal meal, List<String> restrictions) {
        for (String restriction : restrictions) {
            if (!restriction.trim().isEmpty() && meal.matchesRestriction(restriction.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAllergens(Meal meal, List<String> allergies) {
        for (String allergy : allergies) {
            if (!allergy.trim().isEmpty() && meal.containsIngredient(allergy.trim())) {
                return true;
            }
        }
        return false;
    }

    private List<String> generateGroceryList(List<Meal> mealPlan) {
        Set<String> grocerySet = new HashSet<>();
        for (Meal meal : mealPlan) {
            grocerySet.addAll(meal.getIngredients());
        }
        return new ArrayList<>(grocerySet);
    }

    private String getMealPlanDescription(List<Meal> mealPlan, String gender, double weight, double height, double age, String activity, String goal) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Generate a detailed meal plan for a user with the following attributes:\n");
        promptBuilder.append("Gender: ").append(gender).append("\n");
        promptBuilder.append("Age: ").append(age).append("\n");
        promptBuilder.append("Weight: ").append(weight).append(" kg\n");
        promptBuilder.append("Height: ").append(height).append(" cm\n");
        promptBuilder.append("Activity Level: ").append(activity.replace("_", " ")).append("\n");
        promptBuilder.append("Goal: ").append(goal).append("\n");
        promptBuilder.append("Meal Plan:\n");

        for (Meal meal : mealPlan) {
            promptBuilder.append("- ").append(meal.getName()).append("\n");
        }

        promptBuilder.append("Provide detailed nutritional information and explain why these meals are suitable for the user's goal.");

        String prompt = promptBuilder.toString();

        String apiKey = System.getenv("OPENAI_API_KEY"); 
        String response = callOpenAIAPI(prompt, apiKey);

        return response;
    }
