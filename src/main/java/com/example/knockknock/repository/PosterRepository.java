package com.example.knockknock.repository;

import com.example.knockknock.entity.Poster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PosterRepository extends JpaRepository<Poster, Long> {
    @Query("SELECT p FROM Poster p WHERE p.indieMovie.id = :indieId")
    List<Poster> findByIndieId(Long indieId);
}
