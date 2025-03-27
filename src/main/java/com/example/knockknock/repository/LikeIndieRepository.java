package com.example.knockknock.repository;

import com.example.knockknock.entity.IndieMovie;
import com.example.knockknock.entity.LikeIndie;
import com.example.knockknock.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LikeIndieRepository extends JpaRepository<LikeIndie, Long> {
    @Query("SELECT l FROM LikeIndie l WHERE l.indieMovie.id = :indieId AND l.user.id = :userId")
    Optional<LikeIndie> findByIndieMovieIdAndUserId(Long indieId, Long userId);
}
