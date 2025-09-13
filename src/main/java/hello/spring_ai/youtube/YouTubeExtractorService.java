package hello.spring_ai.youtube;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class YouTubeExtractorService {

    private static final Pattern VIDEO_ID_PATTERN = Pattern.compile("(?:youtube\\.com/(?:watch\\?v=|shorts/)|youtu\\.be/)([a-zA-Z0-9_-]{11})");
    private static final String YOUTUBE_WATCH_URL = "https://www.youtube.com/watch?v=";

    public YouTubeVideoInfo extractVideoInfo(String url) throws IOException {
        String videoId = extractVideoId(url);
        if (videoId == null) {
            throw new IllegalArgumentException("유효하지 않은 유튜브 URL입니다.");
        }

        String normalizedUrl = YOUTUBE_WATCH_URL + videoId;
        
        // 유튜브 페이지에서 메타데이터 추출
        Document document = Jsoup.connect(normalizedUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get();

        String title = extractTitle(document);
        String description = extractDescription(document);
        
        return new YouTubeVideoInfo(videoId, title, description, normalizedUrl);
    }

    private String extractVideoId(String url) {
        Matcher matcher = VIDEO_ID_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractTitle(Document document) {
        // 메타 태그에서 제목 추출
        Element titleMeta = document.selectFirst("meta[property=og:title]");
        if (titleMeta != null) {
            return titleMeta.attr("content");
        }
        
        // title 태그에서 제목 추출 (fallback)
        Element titleElement = document.selectFirst("title");
        if (titleElement != null) {
            String title = titleElement.text();
            return title.replace(" - YouTube", "");
        }
        
        return "제목 없음";
    }

    private String extractDescription(Document document) {
        // 메타 태그에서 설명 추출
        Element descMeta = document.selectFirst("meta[property=og:description]");
        if (descMeta != null) {
            return descMeta.attr("content");
        }
        
        Element descriptionMeta = document.selectFirst("meta[name=description]");
        if (descriptionMeta != null) {
            return descriptionMeta.attr("content");
        }
        
        return "설명 없음";
    }

    public static class YouTubeVideoInfo {
        private final String videoId;
        private final String title;
        private final String description;
        private final String url;

        public YouTubeVideoInfo(String videoId, String title, String description, String url) {
            this.videoId = videoId;
            this.title = title;
            this.description = description;
            this.url = url;
        }

        public String getVideoId() { return videoId; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getUrl() { return url; }
        
        public String getContentForSummary() {
            return String.format("제목: %s\n\n설명: %s", title, description);
        }
    }
}