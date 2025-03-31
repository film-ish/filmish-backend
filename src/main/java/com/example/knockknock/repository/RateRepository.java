package com.example.knockknock.repository;

import com.example.knockknock.entity.Rate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RateRepository extends JpaRepository<Rate, Long> {
    @Query("SELECT r FROM Rate r WHERE r.user.id = :userId")
    Page<Rate> findByUserId(Long userId, Pageable pageable);
}
