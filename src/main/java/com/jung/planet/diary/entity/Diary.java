package com.jung.planet.diary.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.report.entity.Report;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;


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
    private Boolean isPublic;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = true)
    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "reportedDiary", cascade = CascadeType.ALL)
    private Set<Report> reports;

    @Builder
    public Diary(Plant plant, Boolean isPublic, String imgUrl, String content, LocalDateTime createdAt) {
        this.plant = plant;
        this.isPublic = isPublic;
        this.imgUrl = imgUrl;
        this.content = content;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now(); // 조건부 설정
    }


    public void setPublic(Boolean aPublic) {
        isPublic = aPublic;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
