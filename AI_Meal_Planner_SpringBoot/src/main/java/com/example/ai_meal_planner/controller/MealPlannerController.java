









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