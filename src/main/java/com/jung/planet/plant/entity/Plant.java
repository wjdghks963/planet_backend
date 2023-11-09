package com.jung.planet.plant.entity;

import com.jung.planet.diary.entity.Diary;
import com.jung.planet.user.entity.User;
import com.jung.planet.model.UserPlantHeart;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "plant")
public class Plant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "nick_name", nullable = false)
    private String nickName;

    @Column(name = "scientific_name", nullable = false)
    private String scientificName;

    @Column(name = "img_url", nullable = false, columnDefinition = "TEXT")
    private String imgUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Diary> diaries;

    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserPlantHeart> hearts;


    @Builder
    public Plant(User user, String nickName, String scientificName, String imgUrl) {
        this.user = user;
        this.nickName = nickName;
        this.scientificName = scientificName;
        this.imgUrl = imgUrl;
        this.createdAt = LocalDateTime.now();
    }
}
