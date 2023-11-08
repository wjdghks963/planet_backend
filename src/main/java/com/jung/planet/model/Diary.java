package com.jung.planet.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "diary")
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Boolean isPublic;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
