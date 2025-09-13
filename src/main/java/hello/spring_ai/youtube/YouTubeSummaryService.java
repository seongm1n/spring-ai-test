package hello.spring_ai.youtube;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
public class YouTubeSummaryService {

    private final ChatClient chatClient;
    private final YouTubeExtractorService extractorService;

    public YouTubeSummaryService(ChatClient.Builder builder, YouTubeExtractorService extractorService) {
        this.extractorService = extractorService;
        this.chatClient = builder
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4o")
                        .temperature(0.3)
                        .build())
                .defaultSystem("""
                        너는 유튜브 동영상 요약 전문가야.
                        주어진 유튜브 동영상의 제목과 설명을 바탕으로 핵심 내용을 요약해줘.
                        
                        요약 규칙:
                        1. 한국어로 답변해줘
                        2. 3-5개의 주요 포인트로 요약해줘
                        3. 각 포인트는 간결하고 명확하게 작성해줘
                        4. 동영상의 주제와 핵심 메시지를 포함해줘
                        5. 불필요한 정보는 제외하고 핵심만 추출해줘
                        
                        응답 형식:
                        📺 **동영상 요약**
                        
                        **주요 내용:**
                        • [포인트 1]
                        • [포인트 2]
                        • [포인트 3]
                        
                        **핵심 메시지:**
                        [동영상의 주요 메시지나 결론]
                        """)
                .build();
    }

    public YouTubeResponse summarizeVideo(String url) {
        try {
            // 유튜브 동영상 정보 추출
            YouTubeExtractorService.YouTubeVideoInfo videoInfo = extractorService.extractVideoInfo(url);
            
            // AI를 사용하여 요약 생성
            String summary = chatClient
                    .prompt()
                    .user(String.format("""
                            다음 유튜브 동영상을 요약해줘:
                            
                            %s
                            """, videoInfo.getContentForSummary()))
                    .call()
                    .content();

            return YouTubeResponse.success(summary, videoInfo.getTitle(), videoInfo.getUrl());
            
        } catch (IllegalArgumentException e) {
            return YouTubeResponse.failure("유효하지 않은 유튜브 URL입니다: " + e.getMessage());
        } catch (Exception e) {
            return YouTubeResponse.failure("유튜브 동영상을 요약하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}