package com.jung.planet.r2;

import com.jung.planet.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Component
public class CloudflarePurgeCache {

    private static final Logger logger = LoggerFactory.getLogger(CloudflarePurgeCache.class);

    private final String cloudflareXAuthEmail;
    private final String cloudflareXAuthKey;

    private final String purgeEndpoint;

    public CloudflarePurgeCache(@Value("${cloudflareR2.email}") String cloudflareXAuthEmail, @Value("${cloudflareR2.global_api_key}") String cloudflareXAuthKey, @Value("${cloudflareR2.zone_id}") String zoneId) {
        this.cloudflareXAuthEmail = cloudflareXAuthEmail;
        this.cloudflareXAuthKey = cloudflareXAuthKey;
        this.purgeEndpoint = "https://api.cloudflare.com/client/v4/zones/" + zoneId + "/purge_cache";
    }

    /**
     * Cloudflare 캐시를 purge합니다.
     *
     * @param imageUrl 캐시에서 제거할 이미지 URL
     * @throws ExternalServiceException Cloudflare API 호출 실패 시
     */
    void purgeCache(String imageUrl) {
        try {
            logger.debug("Cloudflare 캐시 purge 시작. imageUrl: {}", imageUrl);

            URL url = new URL(purgeEndpoint);
            HttpURLConnection conn = getHttpURLConnection(imageUrl, url);

            int responseCode = conn.getResponseCode();

            if (responseCode >= 200 && responseCode < 300) {
                logger.info("Cloudflare 캐시 purge 성공. imageUrl: {}, responseCode: {}", imageUrl, responseCode);
            } else {
                logger.warn("Cloudflare 캐시 purge 응답 코드 비정상. imageUrl: {}, responseCode: {}", imageUrl, responseCode);
            }

        } catch (Exception e) {
            logger.error("Cloudflare 캐시 purge 실패. imageUrl: {}", imageUrl, e);
            throw new ExternalServiceException("Cloudflare Cache", "캐시 purge", e);
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
