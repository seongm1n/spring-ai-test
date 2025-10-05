package hello.spring_ai.reading;

/**
 * 모델별 읽기 자료 생성 결과
 */
public record ModelComparisonResponse(
        String modelName,
        String content,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens,
        double generationTimeMs
) {
}
