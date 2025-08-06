package hello.spring_ai.review;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/summary")
public class SummaryController {

    private final ReviewSummaryService summaryService;

    public SummaryController(ReviewSummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping("/reviews")
    public ResponseEntity<SummaryResponse> getAllReviewsSummary() {
        SummaryResponse summary = summaryService.summarizeAllReviews();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/reviews/recent")
    public ResponseEntity<SummaryResponse> getRecentReviewsSummary(
            @RequestParam(defaultValue = "10") int limit) {
        
        if (limit <= 0 || limit > 100) {
            limit = 10; // 기본값으로 설정
        }
        
        SummaryResponse summary = summaryService.summarizeRecentReviews(limit);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/reviews/rating/{rating}")
    public ResponseEntity<SummaryResponse> getReviewsSummaryByRating(
            @PathVariable int rating) {
        
        if (rating < 1 || rating > 5) {
            return ResponseEntity.badRequest().build();
        }
        
        SummaryResponse summary = summaryService.summarizeByRating(rating);
        return ResponseEntity.ok(summary);
    }
}