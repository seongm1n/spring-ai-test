package hello.spring_ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 여러 OpenAI 모델을 사용하기 위한 설정 클래스
 * GPT-3.5 Turbo, GPT-4, GPT-4 Turbo 모델별로 별도의 ChatClient Bean을 생성
 */
@Configuration
public class MultiModelConfig {

    /**
     * GPT-3.5 Turbo 모델용 ChatClient
     * - 빠른 응답 속도
     * - 저렴한 비용
     * - 기본적인 텍스트 생성에 적합
     */
    @Bean("gpt35Client")
    public ChatClient gpt35Client(ChatClient.Builder builder) {
        return builder
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-3.5-turbo")
                        .build())
                .build();
    }

    /**
     * GPT-4 모델용 ChatClient
     * - 높은 품질의 텍스트 생성
     * - 복잡한 작업 처리 가능
     * - GPT-3.5보다 느리고 비용이 높음
     */
    @Bean("gpt4Client")
    public ChatClient gpt4Client(ChatClient.Builder builder) {
        return builder
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4")
                        .build())
                .build();
    }

    /**
     * GPT-4 Turbo 모델용 ChatClient
     * - GPT-4의 품질 유지
     * - 더 빠른 응답 속도
     * - GPT-4보다 저렴한 비용
     */
    @Bean("gpt4TurboClient")
    public ChatClient gpt4TurboClient(ChatClient.Builder builder) {
        return builder
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4-turbo-preview")
                        .build())
                .build();
    }
}
