package hello.spring_ai.review;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Transactional
    public ReviewResponse saveReview(ReviewRequest request) {
        Review review = new Review(request.author(), request.content(), request.rating());
        Review savedReview = reviewRepository.save(review);
        return ReviewResponse.from(savedReview);
    }

    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ReviewResponse::from)
                .toList();
    }

    public Optional<ReviewResponse> getReview(Long id) {
        return reviewRepository.findById(id)
                .map(ReviewResponse::from);
    }

    public List<ReviewResponse> getReviewsByRating(Integer minRating) {
        return reviewRepository.findByRatingGreaterThanEqual(minRating)
                .stream()
                .map(ReviewResponse::from)
                .toList();
    }

    public List<ReviewResponse> getReviewsByAuthor(String author) {
        return reviewRepository.findByAuthorContainingIgnoreCase(author)
                .stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Transactional
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ReviewNotFoundException("ID " + id + "인 리뷰를 찾을 수 없습니다.");
        }
        reviewRepository.deleteById(id);
    }

    @Transactional
    public ReviewResponse updateReview(Long id, ReviewRequest request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("ID " + id + "인 리뷰를 찾을 수 없습니다."));
        
        review.setAuthor(request.author());
        review.setContent(request.content());
        review.setRating(request.rating());
        
        Review updatedReview = reviewRepository.save(review);
        return ReviewResponse.from(updatedReview);
    }

    public long getReviewCount() {
        return reviewRepository.count();
    }

    // Spring AI 요약에서 사용할 메서드
    public List<Review> getAllReviewsForSummary() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    // 커스텀 예외 클래스
    public static class ReviewNotFoundException extends RuntimeException {
        public ReviewNotFoundException(String message) {
            super(message);
        }
    }
}