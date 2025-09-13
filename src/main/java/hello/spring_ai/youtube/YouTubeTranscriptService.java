package hello.spring_ai.youtube;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class YouTubeTranscriptService {

    private static final Pattern PLAYER_RESPONSE_PATTERN = Pattern.compile("var ytInitialPlayerResponse = (\\{.*?\\});");
    private static final Pattern CAPTION_TRACKS_PATTERN = Pattern.compile("\"captionTracks\":\\[(.*?)\\]");
    private static final Pattern BASE_URL_PATTERN = Pattern.compile("\"baseUrl\":\"(.*?)\"");

    public List<TranscriptSegment> extractTranscript(String videoId) throws IOException {
        String watchUrl = "https://www.youtube.com/watch?v=" + videoId;
        
        // 유튜브 페이지에서 자막 정보 추출
        Document document = Jsoup.connect(watchUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get();

        String pageHtml = document.html();
        
        // 자막 URL 추출
        String captionUrl = extractCaptionUrl(pageHtml);
        if (captionUrl == null) {
            throw new RuntimeException("자막을 찾을 수 없습니다. 자막이 없는 영상이거나 비공개 영상일 수 있습니다.");
        }

        // 자막 데이터 다운로드 및 파싱
        return downloadAndParseTranscript(captionUrl);
    }

    private String extractCaptionUrl(String pageHtml) {
        try {
            // ytInitialPlayerResponse에서 자막 정보 추출
            Matcher playerResponseMatcher = PLAYER_RESPONSE_PATTERN.matcher(pageHtml);
            if (!playerResponseMatcher.find()) {
                return null;
            }

            String playerResponse = playerResponseMatcher.group(1);
            
            // 자막 트랙 찾기
            Matcher captionTracksMatcher = CAPTION_TRACKS_PATTERN.matcher(playerResponse);
            if (!captionTracksMatcher.find()) {
                return null;
            }

            String captionTracks = captionTracksMatcher.group(1);
            
            // 한국어 자막 우선, 없으면 첫 번째 자막 사용
            Matcher baseUrlMatcher = BASE_URL_PATTERN.matcher(captionTracks);
            if (baseUrlMatcher.find()) {
                String baseUrl = baseUrlMatcher.group(1);
                // JSON 이스케이프 문자 처리
                baseUrl = baseUrl.replace("\\u0026", "&").replace("\\/", "/");
                return URLDecoder.decode(baseUrl, StandardCharsets.UTF_8);
            }

        } catch (Exception e) {
            System.err.println("자막 URL 추출 중 오류: " + e.getMessage());
        }
        return null;
    }

    private List<TranscriptSegment> downloadAndParseTranscript(String captionUrl) throws IOException {
        List<TranscriptSegment> segments = new ArrayList<>();
        
        try {
            // 자막 데이터 다운로드
            Document captionDoc = Jsoup.connect(captionUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

            String transcriptXml = captionDoc.html();
            
            // XML에서 자막 추출 (간단한 정규식 파싱)
            Pattern textPattern = Pattern.compile("<text start=\"([^\"]+)\" dur=\"([^\"]+)\"[^>]*>([^<]+)</text>");
            Matcher matcher = textPattern.matcher(transcriptXml);

            while (matcher.find()) {
                double startSeconds = Double.parseDouble(matcher.group(1));
                double durationSeconds = Double.parseDouble(matcher.group(2));
                String text = matcher.group(3)
                        .replace("&quot;", "\"")
                        .replace("&amp;", "&")
                        .replace("&lt;", "<")
                        .replace("&gt;", ">")
                        .replace("&#39;", "'");

                segments.add(new TranscriptSegment(
                        formatTime(startSeconds),
                        formatTime(startSeconds + durationSeconds),
                        startSeconds,
                        durationSeconds,
                        text.trim()
                ));
            }

        } catch (Exception e) {
            throw new IOException("자막 다운로드 및 파싱 중 오류 발생: " + e.getMessage());
        }

        return segments;
    }

    private String formatTime(double seconds) {
        int minutes = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%d:%02d", minutes, secs);
    }

    public static class TranscriptSegment {
        private final String startTime;
        private final String endTime;
        private final double startSeconds;
        private final double durationSeconds;
        private final String text;

        public TranscriptSegment(String startTime, String endTime, double startSeconds, double durationSeconds, String text) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.startSeconds = startSeconds;
            this.durationSeconds = durationSeconds;
            this.text = text;
        }

        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
        public double getStartSeconds() { return startSeconds; }
        public double getDurationSeconds() { return durationSeconds; }
        public String getText() { return text; }

        @Override
        public String toString() {
            return String.format("[%s] %s", startTime, text);
        }
    }
}