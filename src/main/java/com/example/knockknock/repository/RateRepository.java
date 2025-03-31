package com.example.knockknock.repository;

import com.example.knockknock.entity.Rate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RateRepository extends JpaRepository<Rate, Long> {
    @Query(value = "SELECT r FROM Review r WHERE r.user.id = :userId",
            countQuery = "SELECT COUNT(r) FROM Review r WHERE r.user.id = :userId")
    Page<Rate> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
