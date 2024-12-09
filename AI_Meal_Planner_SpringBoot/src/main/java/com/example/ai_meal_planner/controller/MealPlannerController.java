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