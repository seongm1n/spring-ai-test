package hello.spring_ai.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4o")
                        .temperature(0.7)
                        .build())
                .defaultSystem("""
                        내 이름은 테오야.
                        앞으로 모든 답변은 한국어로 해줘.
                        """)
                .build();
    }

    public ChatResponse chat(ChatRequest request) {
        try {
            String content = chatClient
                    .prompt(request.message())
                    .call()
                    .content();

            return new ChatResponse(content);
        } catch (Exception e) {
            return new ChatResponse("AI 응답을 생성하는 중 오류가 발생했습니다.");
        }
    }
}
