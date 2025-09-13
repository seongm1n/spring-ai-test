package hello.spring_ai.youtube;

public record YouTubeResponse(
        String summary,
        String title,
        String url,
        boolean success,
        String errorMessage
) {
    public static YouTubeResponse success(String summary, String title, String url) {
        return new YouTubeResponse(summary, title, url, true, null);
    }
    
    public static YouTubeResponse failure(String errorMessage) {
        return new YouTubeResponse(null, null, null, false, errorMessage);
    }
}