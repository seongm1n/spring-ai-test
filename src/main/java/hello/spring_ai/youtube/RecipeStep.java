package hello.spring_ai.youtube;

public record RecipeStep(
        int stepNumber,
        String description,
        String startTime,
        String endTime,
        String duration,
        String tips
) {
    public static RecipeStep of(int stepNumber, String description, String startTime, String endTime, String duration, String tips) {
        return new RecipeStep(stepNumber, description, startTime, endTime, duration, tips);
    }
    
    public static RecipeStep simple(int stepNumber, String description) {
        return new RecipeStep(stepNumber, description, null, null, null, null);
    }
}