package hello.spring_ai.reading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 한국어 읽기 자료 생성 서비스
 * 사용자 수준에 맞춰 다양한 GPT 모델로 학습 자료를 생성하고 토큰 사용량을 측정
 */
@Service
public class ReadingMaterialService {

    private static final Logger log = LoggerFactory.getLogger(ReadingMaterialService.class);

    private final Map<String, ChatClient> modelClients;

    public ReadingMaterialService(
            @Qualifier("gpt35Client") ChatClient gpt35Client,
            @Qualifier("gpt4Client") ChatClient gpt4Client,
            @Qualifier("gpt4TurboClient") ChatClient gpt4TurboClient
    ) {
        this.modelClients = Map.of(
                "gpt-3.5-turbo", gpt35Client,
                "gpt-4", gpt4Client,
                "gpt-4-turbo-preview", gpt4TurboClient
        );
    }

    /**
     * 지정된 모델로 읽기 자료 생성
     *
     * @param modelName 사용할 모델 이름 (gpt-3.5-turbo, gpt-4, gpt-4-turbo-preview)
     * @param request   자료 생성 요청 정보
     * @return 생성된 자료 및 토큰 사용량 정보
     */
    public ModelComparisonResponse generateWithModel(String modelName, ReadingMaterialRequest request) {
        ChatClient client = modelClients.get(modelName);
        if (client == null) {
            throw new IllegalArgumentException("지원하지 않는 모델입니다: " + modelName);
        }

        log.info("모델 {}로 읽기 자료 생성 시작 - 수준: {}, 주제: {}", modelName, request.level(), request.topic());

        // 1. 수준별 시스템 프롬프트 구성
        String systemPrompt = buildSystemPrompt(request.level());

        // 2. 사용자 프롬프트 구성
        String userPrompt = buildUserPrompt(request);

        // 3. ChatClient 호출 및 토큰 사용량 측정
        long startTime = System.currentTimeMillis();

        try {
            ChatResponse response = client.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .chatResponse();

            long generationTime = System.currentTimeMillis() - startTime;

            // 4. 토큰 사용량 추출
            var usage = response.getMetadata().getUsage();
            String content = response.getResult().getOutput().getText();

            log.info("모델 {} 생성 완료 - 시간: {}ms, 토큰: {}", modelName, generationTime, usage.getTotalTokens());

            // Spring AI 1.0.0-M6에서는 getGenerationTokens() 사용 (deprecated이지만 현재 사용 가능)
            return new ModelComparisonResponse(
                    modelName,
                    content,
                    usage.getPromptTokens().intValue(),
                    usage.getGenerationTokens().intValue(),
                    usage.getTotalTokens().intValue(),
                    generationTime
            );

        } catch (Exception e) {
            log.error("모델 {}로 자료 생성 실패", modelName, e);
            throw new RuntimeException("읽기 자료 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자 수준에 맞는 시스템 프롬프트 생성
     */
    private String buildSystemPrompt(KoreanLevel level) {
        return switch (level) {
            case BEGINNER -> """
                    당신은 한국어 초급 학습자를 위한 교육 콘텐츠 제작자입니다.

                    작성 규칙:
                    - 기본 문법만 사용 (현재형, 과거형, 기본 조사)
                    - 짧고 간단한 문장 구성 (한 문장당 10-15자)
                    - 일상생활 관련 내용 (가족, 음식, 학교, 취미 등)
                    - 한자어나 외래어 사용 최소화
                    - 기본 동사와 형용사 위주

                    목표: 초급 학습자가 쉽게 읽고 이해할 수 있는 자연스러운 한국어 글
                    """;

            case INTERMEDIATE -> """
                    당신은 한국어 중급 학습자를 위한 교육 콘텐츠 제작자입니다.

                    작성 규칙:
                    - 다양한 문법 구조 사용 가능 (연결어미, 종결어미, 간접화법 등)
                    - 중급 어휘 및 관용 표현 포함
                    - 사회, 문화, 직장 생활 등 다양한 주제
                    - 복문과 중문 사용 가능
                    - 한자어와 외래어 적절히 사용

                    목표: 중급 학습자의 어휘력과 표현력을 향상시키는 흥미로운 글
                    """;

            case ADVANCED -> """
                    당신은 한국어 고급 학습자를 위한 교육 콘텐츠 제작자입니다.

                    작성 규칙:
                    - 복잡한 문법 및 고급 어휘 사용
                    - 추상적 개념 및 전문 용어 포함
                    - 학술, 시사, 문학, 전문 분야 내용
                    - 긴 문장과 복잡한 문장 구조 사용
                    - 관용구, 속담, 사자성어 활용
                    - 격식체와 비격식체 적절히 조합

                    목표: 고급 학습자가 원어민 수준의 한국어 이해력과 표현력을 갖추도록 돕는 깊이 있는 글
                    """;
        };
    }

    /**
     * 자료 생성을 위한 사용자 프롬프트 생성
     */
    private String buildUserPrompt(ReadingMaterialRequest request) {
        return String.format("""
                다음 조건으로 한국어 읽기 자료를 작성해주세요:

                주제: %s
                반드시 포함할 단어: %s
                목표 길이: 약 %d자

                요구사항:
                1. 지정된 단어들을 자연스럽게 모두 포함시켜주세요
                2. 주제에 맞는 흥미롭고 교육적인 내용으로 작성해주세요
                3. 문법적으로 정확하고 자연스러운 한국어를 사용해주세요
                4. 학습자가 읽기 연습과 어휘 학습을 동시에 할 수 있도록 해주세요

                제목과 본문을 포함하여 작성해주세요.
                """,
                request.topic(),
                String.join(", ", request.targetWords()),
                request.targetLength()
        );
    }
}
