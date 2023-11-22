package com.jung.planet.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "subscription")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private SubscriptionType type;

    @Column(nullable = false)
    private int maxPlants;

    @Column(nullable = false)
    private boolean aiServiceAccess;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // 구독 시작
    public void startSubscription() {
        if (this.type != SubscriptionType.BASIC) {
            this.startDate = LocalDate.now();
            this.endDate = this.startDate.plusMonths(1);
        }
    }

    // 유효한 구독인지 확인하는 메소드
    public boolean isValidSubscription() {
        if (this.type == SubscriptionType.BASIC) {
            return true;
        }
        return this.startDate != null && this.endDate != null
                && !LocalDate.now().isBefore(this.startDate)
                && !LocalDate.now().isAfter(this.endDate);
    }

    @Builder
    public Subscription(SubscriptionType type, int maxPlants, boolean aiServiceAccess, User user) {
        this.type = type;
        this.maxPlants = maxPlants;
        this.aiServiceAccess = aiServiceAccess;
        this.user = user;
    }
}

