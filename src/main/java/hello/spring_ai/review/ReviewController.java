package hello.spring_ai.review;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.saveReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getAllReviews(
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) String author) {
        
        List<ReviewResponse> reviews;
        
        if (minRating != null) {
            reviews = reviewService.getReviewsByRating(minRating);
        } else if (author != null) {
            reviews = reviewService.getReviewsByAuthor(author);
        } else {
            reviews = reviewService.getAllReviews();
        }
        
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable Long id) {
        Optional<ReviewResponse> review = reviewService.getReview(id);
        return review.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long id, 
            @Valid @RequestBody ReviewRequest request) {
        try {
            ReviewResponse response = reviewService.updateReview(id, request);
            return ResponseEntity.ok(response);
        } catch (ReviewService.ReviewNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.noContent().build();
        } catch (ReviewService.ReviewNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getReviewCount() {
        long count = reviewService.getReviewCount();
        return ResponseEntity.ok(count);
    }
}