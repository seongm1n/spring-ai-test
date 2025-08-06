package hello.spring_ai.review;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewSummaryService {

    private final ChatClient chatClient;
    private final ReviewService reviewService;

    public ReviewSummaryService(ChatClient.Builder builder, ReviewService reviewService) {
        this.chatClient = builder
                .defaultSystem("""
                        당신은 고객 리뷰 분석 전문가입니다.
                        여러 개의 리뷰를 읽고 핵심 내용을 한 문장으로 요약해주세요.
                        감정, 평점, 주요 키워드를 고려하여 전체적인 평가를 포함해주세요.
                        응답은 반드시 한국어로 해주세요.
                        """)
                .build();
        this.reviewService = reviewService;
    }

    public SummaryResponse summarizeAllReviews() {
        List<Review> reviews = reviewService.getAllReviewsForSummary();

        if (reviews.isEmpty()) {
            return SummaryResponse.of("등록된 리뷰가 없습니다.", 0, 0.0);
        }

        // 통계 계산
        int totalReviews = reviews.size();
        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // 리뷰 텍스트 구성
        String reviewsText = reviews.stream()
                .map(review -> String.format(
                        "작성자: %s, 평점: %d/5점, 내용: %s",
                        review.getAuthor(),
                        review.getRating(),
                        review.getContent()
                ))
                .collect(Collectors.joining("\n"));

        // AI 요약 프롬프트
        String prompt = String.format("""
                다음 %d개의 고객 리뷰를 분석하여 한 문장으로 요약해주세요:
                
                %s
                
                요약 형식: 전체적으로 [긍정적/부정적/보통] 평가이며, [주요 키워드]에 대한 의견이 많습니다.
                평점 평균: %.1f점
                """, totalReviews, reviewsText, averageRating);

        try {
            String summary = chatClient.prompt(prompt).call().content();
            return SummaryResponse.of(summary, totalReviews, averageRating);
            
        } catch (Exception e) {
            // AI 요약 실패 시 기본 통계 요약 제공
            String fallbackSummary = String.format(
                    "총 %d개의 리뷰가 등록되었으며, 평균 평점은 %.1f점입니다.",
                    totalReviews, averageRating
            );
            return SummaryResponse.of(fallbackSummary, totalReviews, averageRating);
        }
    }

    public SummaryResponse summarizeRecentReviews(int limit) {
        List<Review> allReviews = reviewService.getAllReviewsForSummary();
        
        if (allReviews.isEmpty()) {
            return SummaryResponse.of("등록된 리뷰가 없습니다.", 0, 0.0);
        }

        // 최신 리뷰만 선택
        List<Review> recentReviews = allReviews.stream()
                .limit(limit)
                .toList();

        int totalReviews = recentReviews.size();
        double averageRating = recentReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        String reviewsText = recentReviews.stream()
                .map(review -> String.format(
                        "작성자: %s, 평점: %d/5점, 내용: %s",
                        review.getAuthor(),
                        review.getRating(),
                        review.getContent()
                ))
                .collect(Collectors.joining("\n"));

        String prompt = String.format("""
                다음 최신 %d개의 고객 리뷰를 분석하여 한 문장으로 요약해주세요:
                
                %s
                
                최근 고객들의 주요 의견과 전반적인 만족도를 포함해서 요약해주세요.
                """, totalReviews, reviewsText);

        try {
            String summary = chatClient.prompt(prompt).call().content();
            return SummaryResponse.of(summary, totalReviews, averageRating);
            
        } catch (Exception e) {
            String fallbackSummary = String.format(
                    "최근 %d개 리뷰의 평균 평점은 %.1f점입니다.",
                    totalReviews, averageRating
            );
            return SummaryResponse.of(fallbackSummary, totalReviews, averageRating);
        }
    }

    public SummaryResponse summarizeByRating(int targetRating) {
        List<Review> reviews = reviewService.getAllReviewsForSummary().stream()
                .filter(review -> review.getRating() == targetRating)
                .toList();

        if (reviews.isEmpty()) {
            return SummaryResponse.of(
                    String.format("%d점 리뷰가 없습니다.", targetRating), 
                    0, 0.0
            );
        }

        int totalReviews = reviews.size();
        String reviewsText = reviews.stream()
                .map(review -> String.format(
                        "작성자: %s, 내용: %s",
                        review.getAuthor(),
                        review.getContent()
                ))
                .collect(Collectors.joining("\n"));

        String prompt = String.format("""
                다음 %d점 평점을 준 %d개의 고객 리뷰를 분석하여 한 문장으로 요약해주세요:
                
                %s
                
                %d점을 준 고객들의 공통적인 의견과 특징을 요약해주세요.
                """, targetRating, totalReviews, reviewsText, targetRating);

        try {
            String summary = chatClient.prompt(prompt).call().content();
            return SummaryResponse.of(summary, totalReviews, (double) targetRating);
            
        } catch (Exception e) {
            String fallbackSummary = String.format(
                    "%d점 평가를 받은 리뷰가 총 %d개 있습니다.",
                    targetRating, totalReviews
            );
            return SummaryResponse.of(fallbackSummary, totalReviews, (double) targetRating);
        }
    }
}