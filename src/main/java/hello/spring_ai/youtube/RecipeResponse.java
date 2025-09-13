package hello.spring_ai.youtube;

import java.util.List;

public record RecipeResponse(
        String dishName,
        List<String> ingredients,
        List<RecipeStep> steps,
        List<String> instructions, // 하위 호환성을 위해 유지
        String cookingTime,
        String difficulty,
        String servings,
        String url,
        String videoTitle,
        boolean success,
        String errorMessage
) {
    public static RecipeResponse success(
            String dishName, 
            List<String> ingredients, 
            List<RecipeStep> steps,
            List<String> instructions,
            String cookingTime,
            String difficulty,
            String servings,
            String url,
            String videoTitle
    ) {
        return new RecipeResponse(dishName, ingredients, steps, instructions, cookingTime, difficulty, servings, url, videoTitle, true, null);
    }
    
    public static RecipeResponse failure(String errorMessage) {
        return new RecipeResponse(null, null, null, null, null, null, null, null, null, false, errorMessage);
    }
}