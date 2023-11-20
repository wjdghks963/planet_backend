package com.jung.planet.r2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Component
public class CloudFlarePurgeCache {

    private final String cloudflareXAuthEmail;
    private final String cloudflareXAuthKey;

    private final String purgeEndpoint;

    public CloudFlarePurgeCache(@Value("${cloudflareR2.email}") String cloudflareXAuthEmail, @Value("${cloudflareR2.global_api_key}") String cloudflareXAuthKey, @Value("${cloudflareR2.zone_id}") String zoneId) {
        this.cloudflareXAuthEmail = cloudflareXAuthEmail;
        this.cloudflareXAuthKey = cloudflareXAuthKey;
        this.purgeEndpoint = "https://api.cloudflare.com/client/v4/zones/" + zoneId + "/purge_cache";
    }

    // Cloudflare 캐시 purge 메소드
    void purgeCache(String imageUrl) {
        try {
            URL url = new URL(purgeEndpoint);
            HttpURLConnection conn = getHttpURLConnection(imageUrl, url);

            int responseCode = conn.getResponseCode();
            System.out.println("Response Code : " + responseCode);

        } catch (Exception e) {
            // 예외 처리
            throw new RuntimeException("Failed to purge Cloudflare cache", e);
        }
    }

    private HttpURLConnection getHttpURLConnection(String imageUrl, URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-Auth-Email", cloudflareXAuthEmail);
        conn.setRequestProperty("X-Auth-Key", cloudflareXAuthKey);
        conn.setRequestProperty("Content-Type", "application/json");

        String postData = "{\"files\":[\"" + imageUrl + "\"]}";
        conn.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.writeBytes(postData);
        }
        return conn;
    }
}
