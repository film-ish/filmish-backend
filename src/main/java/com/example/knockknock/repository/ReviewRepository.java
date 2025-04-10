package com.example.knockknock.repository;

import com.example.knockknock.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query(value = "SELECT r FROM Review r WHERE r.indieMovie.id = :indieId",
            countQuery = "SELECT COUNT(r) FROM Review r WHERE r.indieMovie.id = :indieId")
    Page<Review> findByIndieId(@Param("indieId") Long indieId, Pageable pageable);

    @Query(value = "SELECT r FROM Review r WHERE r.user.id = :userId",
            countQuery = "SELECT COUNT(r) FROM Review r WHERE r.user.id = :userId")
    Page<Review> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT r from Review r " +
            "WHERE r.user.active = true " +
            "ORDER BY r.views DESC")
    List<Review> findBestReviews(Pageable pageable);
}
