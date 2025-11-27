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

    @Column(name = "created_at", nullable = false, updatable = false)
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

    /**
     * 다이어리 내용을 수정합니다.
     */
    public void updateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("다이어리 내용은 필수입니다.");
        }
        this.content = content;
    }

    /**
     * 다이어리 공개 여부를 변경합니다.
     */
    public void updateVisibility(Boolean isPublic) {
        if (isPublic == null) {
            throw new IllegalArgumentException("공개 여부는 null일 수 없습니다.");
        }
        this.isPublic = isPublic;
    }

    /**
     * 다이어리 이미지 URL을 수정합니다.
     */
    public void updateImageUrl(String imgUrl) {
        if (imgUrl == null || imgUrl.isBlank()) {
            throw new IllegalArgumentException("이미지 URL은 필수입니다.");
        }
        this.imgUrl = imgUrl;
    }
}
