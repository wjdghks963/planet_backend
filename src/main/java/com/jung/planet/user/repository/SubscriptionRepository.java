package com.jung.planet.user.repository;

import com.jung.planet.user.entity.Subscription;
import com.jung.planet.user.entity.SubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByType(SubscriptionType type);

}