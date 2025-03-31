package com.example.knockknock.repository;

import com.example.knockknock.entity.IndieGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IndieGenreRepository extends JpaRepository<IndieGenre, Long> {
    @Query("SELECT ig FROM IndieGenre ig WHERE ig.indieMovie.id = :indieId")
    List<IndieGenre> findByIndieId(Long indieId);
}
