package com.jung.planet.report.entity;

import com.jung.planet.diary.entity.Diary;
import com.jung.planet.plant.entity.Plant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "report")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "plant_id")
    private Plant reportedPlant;

    @ManyToOne
    @JoinColumn(name = "diary_id")
    private Diary reportedDiary;

    private Long reporterId;

}

