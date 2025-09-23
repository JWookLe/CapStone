package com.example.EmotionSync;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class JsonParser {

    // JSON String을 받아서 ContentItem 리스트로 변환하는 메소드
    public static List<ContentItem> parseRecommendations(String responseBody) {
        List<ContentItem> recommendations = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(responseBody);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);

                String type = item.optString("type", "");
                String title = item.optString("title", "");
                String source = item.optString("source", "");
                String imageUrl = item.optString("image_url", "");
                String linkUrl = item.optString("link_url", "");

                recommendations.add(new ContentItem(type, title, source, imageUrl, linkUrl));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return recommendations;
    }
}
