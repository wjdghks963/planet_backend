package com.jung.planet.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jung.planet.plant.entity.Plant;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "refresh_token")
    private String refreshToken;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Subscription subscription;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Plant> plants;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    /**
     * 리프레시 토큰을 업데이트합니다.
     *
     * @param newRefreshToken 새로운 리프레시 토큰
     * @throws IllegalArgumentException 토큰이 null이거나 비어있는 경우
     */
    public void updateRefreshToken(String newRefreshToken) {
        if (newRefreshToken == null || newRefreshToken.isBlank()) {
            throw new IllegalArgumentException("리프레시 토큰은 null이거나 비어있을 수 없습니다.");
        }
        this.refreshToken = newRefreshToken;
    }

    /**
     * 리프레시 토큰을 초기화합니다.
     * 로그아웃 시 사용됩니다.
     */
    public void clearRefreshToken() {
        this.refreshToken = null;
    }

    /**
     * 사용자 역할을 설정합니다.
     *
     * @param role 사용자 역할
     * @throws IllegalArgumentException 역할이 null인 경우
     */
    public void assignRole(UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("사용자 역할은 null일 수 없습니다.");
        }
        this.role = role;
    }

    /**
     * 구독 정보를 설정합니다.
     *
     * @param subscription 구독 정보
     */
    public void attachSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    @Builder(toBuilder = true)
    public User(String email, String name, String refreshToken, Subscription subscription) {
        this.email = email;
        this.name = name;
        this.refreshToken = (refreshToken != null) ? refreshToken : "";
        this.subscription = subscription;
        this.createdAt = LocalDateTime.now();
    }
}
