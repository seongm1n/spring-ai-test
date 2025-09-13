package hello.spring_ai.youtube;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/youtube")
public class YouTubeController {

    private final YouTubeSummaryService youtubeSummaryService;
    private final RecipeExtractionService recipeExtractionService;

    public YouTubeController(YouTubeSummaryService youtubeSummaryService, RecipeExtractionService recipeExtractionService) {
        this.youtubeSummaryService = youtubeSummaryService;
        this.recipeExtractionService = recipeExtractionService;
    }

    @PostMapping("/summarize")
    public ResponseEntity<YouTubeResponse> summarizeVideo(@Valid @RequestBody YouTubeRequest request) {
        YouTubeResponse response = youtubeSummaryService.summarizeVideo(request.url());
        
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/summarize")
    public ResponseEntity<YouTubeResponse> summarizeVideoByUrl(@RequestParam("url") String url) {
        // URL 파라미터로도 접근 가능하도록 GET 메서드 제공
        YouTubeRequest request = new YouTubeRequest(url);
        return summarizeVideo(request);
    }

    @PostMapping("/recipe")
    public ResponseEntity<RecipeResponse> extractRecipe(@Valid @RequestBody YouTubeRequest request) {
        RecipeResponse response = recipeExtractionService.extractRecipe(request.url());
        
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/recipe")
    public ResponseEntity<RecipeResponse> extractRecipeByUrl(@RequestParam("url") String url) {
        // URL 파라미터로도 접근 가능하도록 GET 메서드 제공
        YouTubeRequest request = new YouTubeRequest(url);
        return extractRecipe(request);
    }
}