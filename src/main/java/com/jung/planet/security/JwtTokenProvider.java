package com.jung.planet.security;

import com.jung.planet.plant.controller.PlantController;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final Logger logger = LoggerFactory.getLogger(PlantController.class);
    private final Key key;


    private long validityInMilliseconds = 3600000; // 1h

    @Autowired
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            this.key = Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            logger.error("Failed to set HMAC SHA key: ", e);
            throw e;
        }
    }


    public String createToken(Long userId, String email) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("userId", userId);
        //claims.put("auth", new SimpleGrantedAuthority("ROLE_USER"));

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

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
        } catch (Exception e) {
            logger.info("Token validation failed: {}" + e.getMessage().toString());
        }
        return false;
    }


    // Authentication 객체 생성
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();

        List<GrantedAuthority> authorities = Collections.emptyList();


        CustomUserDetails principal = new CustomUserDetails(
                claims.get("userId", Long.class),
                claims.getSubject(),
                authorities
        );

        return new UsernamePasswordAuthenticationToken(principal, token, Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
    }


    // Decode JWT
    public Claims decodeJwt(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
