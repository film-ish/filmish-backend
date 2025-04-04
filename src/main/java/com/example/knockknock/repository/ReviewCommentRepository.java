package com.example.knockknock.repository;

import com.example.knockknock.entity.ReviewComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {
    @Query(value = "SELECT rc FROM ReviewComment rc WHERE rc.review.id = :reviewId",
            countQuery = "SELECT COUNT(rc) FROM ReviewComment rc WHERE rc.review.id = :reviewId")
    Page<ReviewComment> findByReviewId(@Param("reviewId") Long reviewId, Pageable pageable);

    @Query("SELECT rc FROM ReviewComment rc WHERE rc.parentComment.id = :parentId")
    Optional<List<ReviewComment>> findByParentCommentId(Long parentId);

    @Query(value = "SELECT rc FROM ReviewComment rc WHERE rc.user.id = :userId",
            countQuery = "SELECT COUNT(rc) FROM ReviewComment rc WHERE rc.user.id = :userId")
    Page<ReviewComment> findByUserId(Long userId, Pageable pageable);
}
