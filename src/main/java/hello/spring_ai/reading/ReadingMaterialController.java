package hello.spring_ai.reading;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 한국어 읽기 자료 생성 API
 */
@RestController
@RequestMapping("/api/reading")
public class ReadingMaterialController {

    private static final Logger log = LoggerFactory.getLogger(ReadingMaterialController.class);

    private final ReadingMaterialService readingMaterialService;

    private static final List<String> SUPPORTED_MODELS = List.of(
            "gpt-3.5-turbo",
            "gpt-4",
            "gpt-4-turbo-preview"
    );

    public ReadingMaterialController(ReadingMaterialService readingMaterialService) {
        this.readingMaterialService = readingMaterialService;
    }

    /**
     * 모든 모델로 읽기 자료를 생성하고 비교
     *
     * @param request 자료 생성 요청
     * @return 모델별 생성 결과 목록
     */
    @PostMapping("/generate")
    public ResponseEntity<List<ModelComparisonResponse>> generateComparison(
            @Valid @RequestBody ReadingMaterialRequest request
    ) {
        log.info("모든 모델 비교 요청 - 수준: {}, 주제: {}", request.level(), request.topic());

        List<ModelComparisonResponse> responses = SUPPORTED_MODELS.stream()
                .map(model -> readingMaterialService.generateWithModel(model, request))
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * 특정 모델로만 읽기 자료 생성
     *
     * @param model   사용할 모델 이름
     * @param request 자료 생성 요청
     * @return 생성 결과
     */
    @PostMapping("/generate/{model}")
    public ResponseEntity<ModelComparisonResponse> generateSingle(
            @PathVariable String model,
            @Valid @RequestBody ReadingMaterialRequest request
    ) {
        log.info("단일 모델 요청 - 모델: {}, 수준: {}, 주제: {}", model, request.level(), request.topic());

        if (!SUPPORTED_MODELS.contains(model)) {
            return ResponseEntity.badRequest().build();
        }

        ModelComparisonResponse response = readingMaterialService.generateWithModel(model, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 지원하는 모델 목록 조회
     *
     * @return 사용 가능한 모델 목록
     */
    @GetMapping("/models")
    public ResponseEntity<List<String>> getSupportedModels() {
        return ResponseEntity.ok(SUPPORTED_MODELS);
    }

    /**
     * 예외 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.error("잘못된 요청: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("서버 오류 발생", e);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }

    /**
     * 에러 응답 DTO
     */
    public record ErrorResponse(String message) {
    }
}
