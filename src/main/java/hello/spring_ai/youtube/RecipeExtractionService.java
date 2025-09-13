package hello.spring_ai.youtube;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecipeExtractionService {

    private final ChatClient chatClient;
    private final YouTubeExtractorService extractorService;
    private final YouTubeTranscriptService transcriptService;
    private final ObjectMapper objectMapper;

    public RecipeExtractionService(ChatClient.Builder builder, YouTubeExtractorService extractorService, YouTubeTranscriptService transcriptService) {
        this.extractorService = extractorService;
        this.transcriptService = transcriptService;
        this.objectMapper = new ObjectMapper();
        this.chatClient = builder
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4o")
                        .temperature(0.2)
                        .maxTokens(1500)
                        .build())
                .defaultSystem("""
                        너는 전문 요리사이자 영상 분석 전문가야.
                        주어진 유튜브 요리 영상의 자막(transcript)과 메타데이터를 분석해서 정확한 레시피와 타임스탬프를 추출해줘.
                        
                        분석 데이터:
                        1. 영상 제목과 설명
                        2. 실시간 자막 데이터 (타임스탬프 포함)
                        
                        재료 추출 규칙:
                        1. 자막에서 언급된 모든 재료와 분량을 정확히 추출해줘
                        2. "양파 1개", "간장 2큰술", "소금 조금" 등 구체적인 표현 사용
                        3. 자막에서 분량이 명시되지 않은 재료는 일반적인 분량으로 추정해줘
                        4. 주재료, 부재료, 양념을 구분해서 정리해줘
                        
                        조리과정 및 타임스탬프 추출 규칙:
                        1. 자막의 타임스탬프를 기반으로 실제 조리 단계별 시간을 정확히 찾아줘
                        2. 각 단계가 시작되는 정확한 시간과 끝나는 시간을 자막에서 추출해줘
                        3. "재료 준비", "볶기 시작", "물 넣기", "완성" 등 구체적인 행동을 기준으로 단계를 나눠줘
                        4. 자막에서 요리사가 말하는 팁이나 주의사항도 해당 단계에 포함해줘
                        5. 타임스탬프는 반드시 자막 데이터의 실제 시간을 사용해줘
                        
                        응답은 반드시 다음 JSON 형식으로만 해줘:
                        {
                          "dishName": "요리명",
                          "ingredients": ["주재료 (분량)", "부재료 (분량)", "양념 (분량)"],
                          "steps": [
                            {
                              "stepNumber": 1,
                              "description": "재료 준비: 구체적인 설명",
                              "startTime": "0:00",
                              "endTime": "2:30", 
                              "duration": "2분 30초",
                              "tips": "요리 팁이나 주의사항"
                            },
                            {
                              "stepNumber": 2,
                              "description": "조리 과정: 시간과 방법 포함",
                              "startTime": "2:30",
                              "endTime": "8:45",
                              "duration": "6분 15초", 
                              "tips": "불 조절 방법"
                            }
                          ],
                          "instructions": ["재료 준비: 구체적인 설명", "조리 과정: 시간과 방법 포함"],
                          "cookingTime": "총 조리시간 (예: 30분)",
                          "difficulty": "난이도 (초급/중급/고급)",
                          "servings": "인분 (예: 2-3인분)"
                        }
                        
                        요리나 음식과 전혀 관련이 없는 영상인 경우에만:
                        {
                          "error": "요리 영상이 아닙니다"
                        }
                        """)
                .build();
    }

    public RecipeResponse extractRecipe(String url) {
        try {
            // 유튜브 동영상 정보 추출
            YouTubeExtractorService.YouTubeVideoInfo videoInfo = extractorService.extractVideoInfo(url);
            
            // 자막 데이터 추출
            List<YouTubeTranscriptService.TranscriptSegment> transcript = null;
            String transcriptText = "";
            
            try {
                transcript = transcriptService.extractTranscript(videoInfo.getVideoId());
                transcriptText = formatTranscriptForAI(transcript);
            } catch (Exception e) {
                System.err.println("자막 추출 실패, 메타데이터만 사용: " + e.getMessage());
                transcriptText = "자막 없음 - 제목과 설명만 분석";
            }
            
            // AI를 사용하여 레시피 추출
            String jsonResponse = chatClient
                    .prompt()
                    .user(String.format("""
                            다음 유튜브 요리 영상을 분석해서 정확한 레시피와 타임스탬프를 추출해줘:
                            
                            === 영상 정보 ===
                            %s
                            
                            === 자막 데이터 (타임스탬프 포함) ===
                            %s
                            
                            요구사항:
                            1. 자막에서 언급된 재료와 정확한 분량을 추출해줘
                            2. 자막의 타임스탬프를 기반으로 실제 조리 단계별 시간을 찾아줘
                            3. 각 단계의 정확한 시작시간과 종료시간을 자막에서 추출해줘
                            4. 요리사가 말하는 팁과 주의사항도 포함해줘
                            5. 자막이 없으면 제목과 설명을 바탕으로 일반적인 레시피를 만들어줘
                            
                            반드시 JSON 형식으로만 응답해줘.
                            """, videoInfo.getContentForSummary(), transcriptText))
                    .call()
                    .content();

            // JSON 응답 파싱
            return parseRecipeJson(jsonResponse, videoInfo);
            
        } catch (IllegalArgumentException e) {
            return RecipeResponse.failure("유효하지 않은 유튜브 URL입니다: " + e.getMessage());
        } catch (Exception e) {
            return RecipeResponse.failure("레시피를 추출하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private RecipeResponse parseRecipeJson(String jsonResponse, YouTubeExtractorService.YouTubeVideoInfo videoInfo) {
        try {
            // JSON 응답에서 불필요한 마크다운 제거
            String cleanJson = jsonResponse.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();

            JsonNode jsonNode = objectMapper.readTree(cleanJson);
            
            // 에러 체크
            if (jsonNode.has("error")) {
                return RecipeResponse.failure(jsonNode.get("error").asText());
            }
            
            // 레시피 정보 추출
            String dishName = jsonNode.get("dishName").asText();
            List<String> ingredients = parseStringArray(jsonNode.get("ingredients"));
            List<RecipeStep> steps = parseRecipeSteps(jsonNode.get("steps"));
            List<String> instructions = parseStringArray(jsonNode.get("instructions"));
            String cookingTime = jsonNode.get("cookingTime").asText();
            String difficulty = jsonNode.get("difficulty").asText();
            String servings = jsonNode.get("servings").asText();
            
            return RecipeResponse.success(
                    dishName, 
                    ingredients, 
                    steps,
                    instructions, 
                    cookingTime, 
                    difficulty, 
                    servings,
                    videoInfo.getUrl(), 
                    videoInfo.getTitle()
            );
            
        } catch (JsonProcessingException e) {
            return RecipeResponse.failure("AI 응답을 처리하는 중 오류가 발생했습니다. 다시 시도해주세요.");
        }
    }

    private List<String> parseStringArray(JsonNode arrayNode) {
        List<String> result = new ArrayList<>();
        if (arrayNode != null && arrayNode.isArray()) {
            for (JsonNode item : arrayNode) {
                result.add(item.asText());
            }
        }
        return result;
    }

    private List<RecipeStep> parseRecipeSteps(JsonNode stepsNode) {
        List<RecipeStep> result = new ArrayList<>();
        if (stepsNode != null && stepsNode.isArray()) {
            for (JsonNode stepNode : stepsNode) {
                int stepNumber = stepNode.has("stepNumber") ? stepNode.get("stepNumber").asInt() : 0;
                String description = stepNode.has("description") ? stepNode.get("description").asText() : "";
                String startTime = stepNode.has("startTime") ? stepNode.get("startTime").asText() : null;
                String endTime = stepNode.has("endTime") ? stepNode.get("endTime").asText() : null;
                String duration = stepNode.has("duration") ? stepNode.get("duration").asText() : null;
                String tips = stepNode.has("tips") ? stepNode.get("tips").asText() : null;
                
                result.add(new RecipeStep(stepNumber, description, startTime, endTime, duration, tips));
            }
        }
        return result;
    }

    private String formatTranscriptForAI(List<YouTubeTranscriptService.TranscriptSegment> transcript) {
        if (transcript == null || transcript.isEmpty()) {
            return "자막 없음";
        }

        StringBuilder formatted = new StringBuilder();
        for (YouTubeTranscriptService.TranscriptSegment segment : transcript) {
            formatted.append(String.format("[%s] %s\n", segment.getStartTime(), segment.getText()));
        }
        
        return formatted.toString();
    }
}
