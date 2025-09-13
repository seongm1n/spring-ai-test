package hello.spring_ai.youtube;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record YouTubeRequest(
        @NotBlank(message = "유튜브 URL은 필수입니다.")
        @Pattern(
                regexp = "^(https?://)?(www\\.)?(youtube\\.com/(watch\\?v=|shorts/)|youtu\\.be/)[a-zA-Z0-9_-]{11}.*$",
                message = "올바른 유튜브 URL을 입력해주세요."
        )
        String url
) {
}