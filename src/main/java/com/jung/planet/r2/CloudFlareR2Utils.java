package com.jung.planet.r2;

import com.jung.planet.exception.ExternalServiceException;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class CloudFlareR2Utils {

    /**
     * 이미지 버퍼의 SHA-256 해시값을 계산합니다.
     *
     * @param imageBuffer 이미지 버퍼
     * @return Base64로 인코딩된 해시값
     */
    public String calculateImageHash(ByteBuffer imageBuffer) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(imageBuffer.array());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new ExternalServiceException("이미지 해시 계산 중 오류가 발생했습니다.", e);
        }
    }
}
