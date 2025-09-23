package com.example.EmotionSync;

import java.util.ArrayList;
import java.util.List;

public class ContentItemFilter {

    // 영화만 걸러내는 메소드
    public static List<ContentItem> filterMovies(List<ContentItem> items) {
        List<ContentItem> movies = new ArrayList<>();
        for (ContentItem item : items) {
            if ("movie".equalsIgnoreCase(item.getType())) {
                movies.add(item);
            }
        }
        return movies;
    }

    // 음악만 걸러내는 메소드
    public static List<ContentItem> filterMusic(List<ContentItem> items) {
        List<ContentItem> music = new ArrayList<>();
        for (ContentItem item : items) {
            if ("music".equalsIgnoreCase(item.getType())) {
                music.add(item);
            }
        }
        return music;
    }
}
