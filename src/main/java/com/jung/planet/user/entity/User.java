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


    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }








    public void setSubscription(Subscription subscription) {
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
