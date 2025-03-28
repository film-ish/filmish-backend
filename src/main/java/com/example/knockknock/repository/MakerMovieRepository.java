package com.example.knockknock.repository;

import com.example.knockknock.entity.MakerMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MakerMovieRepository extends JpaRepository<MakerMovie, Long> {
    @Query("SELECT m FROM MakerMovie m WHERE m.indieMovie.id = :indieId")
    Optional<List<MakerMovie>> findByIndieId(Long indieId);
}
