package hello.spring_ai.reading;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * 한국어 읽기 자료 생성 요청
 */
public record ReadingMaterialRequest(
        @NotNull(message = "수준은 필수입니다")
        KoreanLevel level,

        @NotEmpty(message = "목표 단어는 최소 1개 이상이어야 합니다")
        List<String> targetWords,

        @NotNull(message = "주제는 필수입니다")
        String topic,

        @Positive(message = "목표 길이는 양수여야 합니다")
        int targetLength
) {
}
