package hello.spring_ai.review;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataLoader {

    private final ReviewRepository reviewRepository;

    public DataLoader(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadTestData() {
        // 기존에 데이터가 있다면 로딩하지 않음
        if (reviewRepository.count() > 0) {
            return;
        }

        List<Review> testReviews = Arrays.asList(
                new Review("김민수", "정말 좋은 제품이에요! 품질도 훌륭하고 배송도 빨랐습니다. 강력 추천합니다.", 5),
                new Review("이영희", "배송이 조금 늦었지만 제품 품질은 정말 만족스러워요. 다음에도 구매할 의향이 있습니다.", 4),
                new Review("박철수", "가격 대비 괜찮은 것 같아요. 기대했던 것보다는 조금 아쉽지만 나쁘지 않네요.", 3),
                new Review("최지혜", "포장이 너무 허술했어요. 제품은 괜찮지만 배송 과정에서 상자가 찌그러져서 왔습니다.", 2),
                new Review("정동진", "완전히 기대 이하였습니다. 사진과 실물이 너무 달라서 실망했어요. 환불하고 싶습니다.", 1),
                new Review("홍길동", "생각보다 품질이 좋네요! 디자인도 마음에 들고 사용하기 편합니다. 추천해요.", 5),
                new Review("김영수", "무난한 제품이에요. 특별히 나쁘지도 좋지도 않은 것 같습니다. 보통 수준이네요.", 3),
                new Review("윤지현", "아주 만족합니다! 친구들에게도 추천했어요. 가격 대비 정말 훌륭한 제품입니다.", 5),
                new Review("신민철", "배송은 빨랐는데 제품에 약간의 흠집이 있었어요. 교환해달라고 요청했습니다.", 2),
                new Review("오수진", "전체적으로 만족스럽습니다. 몇 가지 아쉬운 점이 있지만 대체로 좋은 제품이에요.", 4)
        );

        reviewRepository.saveAll(testReviews);
        System.out.println("✅ 테스트 리뷰 데이터 " + testReviews.size() + "개가 로드되었습니다.");
    }
}