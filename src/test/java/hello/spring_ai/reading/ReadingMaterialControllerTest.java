package hello.spring_ai.reading;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ReadingMaterialController 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
class ReadingMaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("지원 모델 목록 조회")
    void getSupportedModels() throws Exception {
        mockMvc.perform(get("/api/reading/models"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]", is("gpt-3.5-turbo")))
                .andExpect(jsonPath("$[1]", is("gpt-4")))
                .andExpect(jsonPath("$[2]", is("gpt-4-turbo-preview")));
    }

    @Test
    @DisplayName("단일 모델로 읽기 자료 생성 - 성공")
    void generateSingleModel() throws Exception {
        var request = new ReadingMaterialRequest(
                KoreanLevel.BEGINNER,
                List.of("사과", "바나나"),
                "과일",
                150
        );

        mockMvc.perform(post("/api/reading/generate/gpt-3.5-turbo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelName", is("gpt-3.5-turbo")))
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.totalTokens", greaterThan(0)))
                .andExpect(jsonPath("$.promptTokens", greaterThan(0)))
                .andExpect(jsonPath("$.completionTokens", greaterThan(0)))
                .andExpect(jsonPath("$.generationTimeMs", greaterThan(0.0)));
    }

    @Test
    @DisplayName("모든 모델 비교 - 성공")
    void generateComparison() throws Exception {
        var request = new ReadingMaterialRequest(
                KoreanLevel.INTERMEDIATE,
                List.of("한국", "문화"),
                "한국 문화",
                200
        );

        mockMvc.perform(post("/api/reading/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].modelName", is("gpt-3.5-turbo")))
                .andExpect(jsonPath("$[1].modelName", is("gpt-4")))
                .andExpect(jsonPath("$[2].modelName", is("gpt-4-turbo-preview")))
                .andExpect(jsonPath("$[*].content", everyItem(notNullValue())))
                .andExpect(jsonPath("$[*].totalTokens", everyItem(greaterThan(0))));
    }

    @Test
    @DisplayName("유효하지 않은 요청 - 목표 단어 없음")
    void generateWithoutTargetWords() throws Exception {
        String invalidJson = """
                {
                    "level": "BEGINNER",
                    "targetWords": [],
                    "topic": "테스트",
                    "targetLength": 100
                }
                """;

        mockMvc.perform(post("/api/reading/generate/gpt-3.5-turbo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유효하지 않은 모델명")
    void generateWithInvalidModel() throws Exception {
        var request = new ReadingMaterialRequest(
                KoreanLevel.BEGINNER,
                List.of("테스트"),
                "테스트",
                100
        );

        mockMvc.perform(post("/api/reading/generate/invalid-model")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
