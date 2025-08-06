package hello.spring_ai.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // 평점 기준으로 리뷰 조회 (특정 평점 이상)
    List<Review> findByRatingGreaterThanEqual(Integer rating);
    
    // 작성자명으로 리뷰 검색 (대소문자 무시)
    List<Review> findByAuthorContainingIgnoreCase(String author);
    
    // 평점별로 내림차순 정렬해서 조회
    List<Review> findAllByOrderByRatingDesc();
    
    // 최신순으로 정렬해서 조회
    List<Review> findAllByOrderByCreatedAtDesc();
}