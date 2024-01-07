package com.jung.planet.user.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "grade_up_requests")
public class GradeUpRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime requestTime;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
