package hello.spring_ai.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient.Builder builder;

    public ChatService(ChatClient.Builder builder) {
        this.builder = builder;
    }

    public ChatResponse chat(ChatRequest request) {
        ChatClient chatClient = builder.build();
        String content = chatClient.prompt(request.message()).call().content();
        return new ChatResponse(content);
    }
}
