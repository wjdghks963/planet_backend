package com.jung.planet.security;

import com.jung.planet.exception.AuthenticationException;
import com.jung.planet.exception.ErrorMessages;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import com.jung.planet.user.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private final Key key;

    private final long validityInMilliseconds = 1000 * 60 * 60 * 12; // 12h
    private final long refreshTokenValidityInMilliseconds = 1000 * 60 * 60 * 24 * 7; // 1 week

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            this.key = Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            logger.error("Failed to set HMAC SHA key: ", e);
            throw new AuthenticationException("JWT 키 설정 실패", e);
        }
    }

    public String createAccessToken(Long userId, String email, UserRole userRole) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("userId", userId);
        claims.put("userRole", userRole);

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long userId, String email, UserRole userRole) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("userId", userId);
        claims.put("userRole", userRole);

        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("만료된 JWT 토큰입니다: {}", e.getMessage());
            throw new AuthenticationException(ErrorMessages.JWT_EXPIRED, e);
        } catch (JwtException e) {
            logger.warn("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
            throw new AuthenticationException(ErrorMessages.JWT_INVALID, e);
        } catch (Exception e) {
            logger.warn("JWT 토큰 검증 실패: {}", e.getMessage());
            throw new AuthenticationException(ErrorMessages.AUTHENTICATION_FAILED, e);
        }
    }

    // Authentication 객체 생성
    public Authentication getAuthentication(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();

            List<GrantedAuthority> authorities = Collections.emptyList();

            String userRoleStr = claims.get("userRole", String.class);
            UserRole userRole = UserRole.valueOf(userRoleStr);

            CustomUserDetails principal = new CustomUserDetails(
                    claims.get("userId", Long.class),
                    claims.getSubject(),
                    userRole,
                    authorities
            );

            return new UsernamePasswordAuthenticationToken(principal, token, 
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        } catch (Exception e) {
            logger.error("JWT 토큰에서 인증 정보를 추출하는데 실패했습니다: {}", e.getMessage());
            throw new AuthenticationException(ErrorMessages.AUTHENTICATION_FAILED, e);
        }
    }

    // Decode JWT
    public Claims decodeJwt(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("만료된 JWT 토큰입니다: {}", e.getMessage());
            throw new AuthenticationException(ErrorMessages.JWT_EXPIRED, e);
        } catch (JwtException e) {
            logger.warn("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
            throw new AuthenticationException(ErrorMessages.JWT_INVALID, e);
        } catch (Exception e) {
            logger.warn("JWT 토큰 디코딩 실패: {}", e.getMessage());
            throw new AuthenticationException(ErrorMessages.AUTHENTICATION_FAILED, e);
        }
    }
}
