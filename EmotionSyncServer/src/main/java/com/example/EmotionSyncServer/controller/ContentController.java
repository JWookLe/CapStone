package com.example.EmotionSyncServer.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/content")
public class ContentController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    @Value("${tmdb.api.key}")
    private String tmdbApiKey;

    @GetMapping("/details")
    public ResponseEntity<?> getContentDetails(
            @RequestParam String type,
            @RequestParam String id,
            @RequestParam(required = false) String region
    ) {
        try {
            if (type.equals("music") || type.equals("video")) {
                // YouTube API 호출
                String url = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/videos")
                        .queryParam("part", "snippet,statistics,contentDetails")
                        .queryParam("id", id)
                        .queryParam("key", youtubeApiKey)
                        .build()
                        .toUriString();

                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                Map<String, Object> videoDetails = ((Map<String, Object>) ((java.util.List<?>) response.get("items")).get(0));
                Map<String, Object> snippet = (Map<String, Object>) videoDetails.get("snippet");
                Map<String, Object> statistics = (Map<String, Object>) videoDetails.get("statistics");
                Map<String, Object> contentDetails = (Map<String, Object>) videoDetails.get("contentDetails");
                Map<String, Object> thumbnails = (Map<String, Object>) (snippet != null ? snippet.get("thumbnails") : null);
                String imageUrl = null;
                if (thumbnails != null) {
                    Map<String, Object> high = (Map<String, Object>) thumbnails.get("high");
                    Map<String, Object> medium = (Map<String, Object>) thumbnails.get("medium");
                    Map<String, Object> def = (Map<String, Object>) thumbnails.get("default");
                    if (high != null && high.get("url") != null) imageUrl = (String) high.get("url");
                    else if (medium != null && medium.get("url") != null) imageUrl = (String) medium.get("url");
                    else if (def != null && def.get("url") != null) imageUrl = (String) def.get("url");
                }

                Map<String, Object> details = new HashMap<>();
                details.put("title", snippet != null && snippet.get("title") != null ? snippet.get("title") : "제목 없음");
                details.put("description", snippet != null && snippet.get("description") != null && !((String)snippet.get("description")).isEmpty() ? snippet.get("description") : "설명이 없습니다");
                details.put("image_url", imageUrl);
                details.put("link_url", "https://www.youtube.com/watch?v=" + id);
                details.put("channel_title", snippet != null && snippet.get("channelTitle") != null ? snippet.get("channelTitle") : "알 수 없음");
                details.put("published_at", snippet != null && snippet.get("publishedAt") != null ? snippet.get("publishedAt") : "");
                details.put("view_count", statistics != null && statistics.get("viewCount") != null ? statistics.get("viewCount") : "0");
                details.put("like_count", statistics != null && statistics.get("likeCount") != null ? statistics.get("likeCount") : "0");
                details.put("comment_count", statistics != null && statistics.get("commentCount") != null ? statistics.get("commentCount") : "0");
                details.put("duration", contentDetails != null && contentDetails.get("duration") != null ? contentDetails.get("duration") : "");
                details.put("tags", snippet != null ? snippet.get("tags") : null);

                return ResponseEntity.ok(details);
            } else if (type.equals("movie")) {
                // TMDB API 호출
                String url = UriComponentsBuilder.fromHttpUrl("https://api.themoviedb.org/3/movie/" + id)
                        .queryParam("api_key", tmdbApiKey)
                        .queryParam("language", "ko-KR".equals(region) ? "ko-KR" : "en-US")
                        .queryParam("append_to_response", "credits,videos")
                        .build()
                        .toUriString();

                Map<String, Object> movie = restTemplate.getForObject(url, Map.class);
                Map<String, Object> credits = (Map<String, Object>) movie.get("credits");

                Map<String, Object> details = new HashMap<>();
                details.put("title", movie.get("title"));
                details.put("overview", movie.get("overview"));
                details.put("image_url", movie.get("poster_path") != null ? 
                    "https://image.tmdb.org/t/p/w500" + movie.get("poster_path") : null);
                details.put("backdrop_url", movie.get("backdrop_path") != null ? 
                    "https://image.tmdb.org/t/p/original" + movie.get("backdrop_path") : null);
                details.put("release_date", movie.get("release_date"));
                details.put("runtime", movie.get("runtime"));
                details.put("vote_average", movie.get("vote_average"));
                details.put("genres", ((java.util.List<?>) movie.get("genres")).stream()
                        .map(genre -> ((Map<String, Object>) genre).get("name"))
                        .toList());
                details.put("director", ((java.util.List<?>) credits.get("crew")).stream()
                        .filter(crew -> "Director".equals(((Map<String, Object>) crew).get("job")))
                        .map(crew -> ((Map<String, Object>) crew).get("name"))
                        .findFirst()
                        .orElse(""));
                details.put("cast", ((java.util.List<?>) credits.get("cast")).stream()
                        .limit(5)
                        .map(cast -> ((Map<String, Object>) cast).get("name"))
                        .toList());
                details.put("link_url", "https://www.themoviedb.org/movie/" + id);

                return ResponseEntity.ok(details);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Unsupported content type"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
} 