package com.jung.planet.diary.entity;

import com.jung.planet.plant.entity.Plant;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor
@Table(name = "diary")
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Boolean isPublic;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @Builder
    public Diary(Plant plant, String title, Boolean isPublic, String imgUrl, String content) {
        this.plant = plant;
        this.title = title;
        this.isPublic = isPublic;
        this.imgUrl = imgUrl;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}
