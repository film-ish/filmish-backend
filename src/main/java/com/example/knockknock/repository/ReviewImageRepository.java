package com.example.knockknock.repository;

import com.example.knockknock.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    Optional<List<ReviewImage>> findByReviewId(Long reviewId);
}
