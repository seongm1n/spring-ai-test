package hello.spring_ai.reading;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ReadingMaterialService 테스트
 * 실제 OpenAI API를 호출하므로 API 키가 필요합니다.
 */
@SpringBootTest
class ReadingMaterialServiceTest {

    @Autowired
    private ReadingMaterialService readingMaterialService;

    @Test
    @DisplayName("초급 수준 읽기 자료 생성 - GPT-3.5 Turbo")
    void generateBeginnerMaterialWithGpt35() {
        // Given
        var request = new ReadingMaterialRequest(
                KoreanLevel.BEGINNER,
                List.of("사과", "학교", "친구"),
                "일상생활",
                200
        );

        // When
        var response = readingMaterialService.generateWithModel("gpt-3.5-turbo", request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.modelName()).isEqualTo("gpt-3.5-turbo");
        assertThat(response.content()).isNotBlank();
        assertThat(response.totalTokens()).isPositive();
        assertThat(response.promptTokens()).isPositive();
        assertThat(response.completionTokens()).isPositive();
        assertThat(response.generationTimeMs()).isPositive();

        // 목표 단어 포함 확인
        assertThat(response.content()).contains("사과", "학교", "친구");
    }

    @Test
    @DisplayName("중급 수준 읽기 자료 생성 - GPT-4")
    void generateIntermediateMaterialWithGpt4() {
        // Given
        var request = new ReadingMaterialRequest(
                KoreanLevel.INTERMEDIATE,
                List.of("환경", "기술", "미래"),
                "사회와 기술",
                300
        );

        // When
        var response = readingMaterialService.generateWithModel("gpt-4", request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.modelName()).isEqualTo("gpt-4");
        assertThat(response.content()).isNotBlank();
        assertThat(response.totalTokens()).isPositive();
        assertThat(response.content()).contains("환경", "기술", "미래");
    }

    @Test
    @DisplayName("고급 수준 읽기 자료 생성 - GPT-4 Turbo")
    void generateAdvancedMaterialWithGpt4Turbo() {
        // Given
        var request = new ReadingMaterialRequest(
                KoreanLevel.ADVANCED,
                List.of("철학", "존재", "인식론"),
                "철학과 사유",
                400
        );

        // When
        var response = readingMaterialService.generateWithModel("gpt-4-turbo-preview", request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.modelName()).isEqualTo("gpt-4-turbo-preview");
        assertThat(response.content()).isNotBlank();
        assertThat(response.totalTokens()).isPositive();
        assertThat(response.content()).contains("철학", "존재", "인식론");
    }

    @Test
    @DisplayName("지원하지 않는 모델 사용 시 예외 발생")
    void generateWithUnsupportedModel() {
        // Given
        var request = new ReadingMaterialRequest(
                KoreanLevel.BEGINNER,
                List.of("테스트"),
                "테스트 주제",
                100
        );

        // When & Then
        assertThatThrownBy(() -> readingMaterialService.generateWithModel("invalid-model", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지원하지 않는 모델입니다");
    }

    @Test
    @DisplayName("토큰 사용량 측정 검증")
    void validateTokenUsage() {
        // Given
        var request = new ReadingMaterialRequest(
                KoreanLevel.BEGINNER,
                List.of("고양이", "강아지"),
                "애완동물",
                150
        );

        // When
        var response = readingMaterialService.generateWithModel("gpt-3.5-turbo", request);

        // Then
        assertThat(response.totalTokens())
                .isEqualTo(response.promptTokens() + response.completionTokens());

        System.out.println("=== 토큰 사용량 분석 ===");
        System.out.println("모델: " + response.modelName());
        System.out.println("프롬프트 토큰: " + response.promptTokens());
        System.out.println("생성 토큰: " + response.completionTokens());
        System.out.println("전체 토큰: " + response.totalTokens());
        System.out.println("생성 시간: " + response.generationTimeMs() + "ms");
        System.out.println("생성된 글자 수: " + response.content().length());
        System.out.println("========================");
    }
}
