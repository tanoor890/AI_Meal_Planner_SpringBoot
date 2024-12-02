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

    // Data structures for meal planning
    private Map<String, Meal> meals = new HashMap<>();

    @PostConstruct
    public void init() {
        // Initialize meal data with recipes and ingredients
        initializeMeals();
    }

    @GetMapping("/")
    public String showForm(Model model) {
        return "index";
    }

    @PostMapping("/calculate")
    public String handleCalculation(@RequestParam Map<String, String> params, Model model) {
        // Retrieve parameters
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

        // Calculate BMR and TDEE
        double bmr = calculateBMR(gender, weight, height, age);
        double tdee = calculateTDEE(bmr, activity);

        // Generate meal plan
        List<Meal> mealPlan = generateMealPlan(goal, restrictions, allergies);

        // Generate grocery list
        List<String> groceryList = generateGroceryList(mealPlan);

        // Generate meal plan description using OpenAI API
        String mealPlanDescription = getMealPlanDescription(mealPlan, gender, weight, height, age, activity, goal);

        // Set attributes for the Thymeleaf template
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
        // Process feedback as needed (e.g., save to database or send an email)
        model.addAttribute("message", "Thank you for your feedback!");
        return "index";
    }



