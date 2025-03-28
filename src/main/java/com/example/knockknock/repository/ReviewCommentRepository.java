package com.example.knockknock.repository;

import com.example.knockknock.entity.Review;
import com.example.knockknock.entity.ReviewComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {
    @Query(value = "SELECT c FROM ReviewComment c WHERE c.review.id = :reviewId",
            countQuery = "SELECT COUNT(c) FROM ReviewComment c WHERE c.review.id = :reviewId")
    Page<ReviewComment> findByReviewId(@Param("reviewId") Long reviewId, Pageable pageable);

    @Query("SELECT c FROM ReviewComment c WHERE c.parentComment.id = :parentId")
    Optional<List<ReviewComment>> findByParentCommentId(Long parentId);
}
