package hello.spring_ai.review;

import jakarta.validation.constraints.*;

public record ReviewRequest(
        @NotBlank(message = "작성자는 필수입니다")
        @Size(max = 100, message = "작성자는 100자 이하로 입력해주세요")
        String author,
        
        @NotBlank(message = "리뷰 내용은 필수입니다")
        @Size(max = 1000, message = "리뷰 내용은 1000자 이하로 입력해주세요")
        String content,
        
        @NotNull(message = "평점은 필수입니다")
        @Min(value = 1, message = "평점은 1점 이상이어야 합니다")
        @Max(value = 5, message = "평점은 5점 이하여야 합니다")
        Integer rating
) {}