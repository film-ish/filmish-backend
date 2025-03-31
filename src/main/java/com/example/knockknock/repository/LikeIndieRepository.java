package com.example.knockknock.repository;

import com.example.knockknock.entity.LikeIndie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeIndieRepository extends JpaRepository<LikeIndie, Long> {
    @Query("SELECT l FROM LikeIndie l WHERE l.indieMovie.id = :indieId AND l.user.id = :userId")
    Optional<LikeIndie> findByIndieMovieIdAndUserId(Long indieId, Long userId);

    @Query(value = "SELECT l FROM LikeIndie l WHERE l.user.id = :userId",
            countQuery = "SELECT COUNT(l) FROM LikeIndie l WHERE l.user.id = :userId")
    Page<LikeIndie> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
