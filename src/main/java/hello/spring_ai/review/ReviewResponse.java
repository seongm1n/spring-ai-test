package hello.spring_ai.review;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        String author,
        String content,
        Integer rating,
        LocalDateTime createdAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getAuthor(),
                review.getContent(),
                review.getRating(),
                review.getCreatedAt()
        );
    }
}