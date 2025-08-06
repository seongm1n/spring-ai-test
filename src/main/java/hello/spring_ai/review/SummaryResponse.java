package hello.spring_ai.review;

import java.time.LocalDateTime;

public record SummaryResponse(
        String summary,
        int totalReviews,
        double averageRating,
        LocalDateTime generatedAt
) {
    public static SummaryResponse of(String summary, int totalReviews, double averageRating) {
        return new SummaryResponse(summary, totalReviews, averageRating, LocalDateTime.now());
    }
}