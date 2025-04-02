package com.example.knockknock.repository;

import com.example.knockknock.entity.Poster;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PosterRepository extends JpaRepository<Poster, Long> {
    @Query("SELECT p FROM Poster p WHERE p.indieMovie.id = :indieId")
    Optional<List<Poster>> findByIndieId(Long indieId);

    @Query("SELECT ig.genre.id AS genreId, " +
            "MIN(p.poster) AS posterUrl " +
            "FROM IndieGenre ig " +
            "LEFT JOIN ig.indieMovie im " +
            "LEFT JOIN Poster p ON im.id = p.indieMovie.id " +
            "WHERE ig.genre.id IN :genreIds " +
            "GROUP BY ig.genre.id ")
    List<Object[]> findFirstPosterByGenreIds(@Param("genreIds") List<Long> genreIds);
}
