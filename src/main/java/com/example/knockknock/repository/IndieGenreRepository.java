package com.example.knockknock.repository;

import com.example.knockknock.entity.IndieGenre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IndieGenreRepository extends JpaRepository<IndieGenre, Long> {
    @Query("SELECT ig FROM IndieGenre ig WHERE ig.indieMovie.id = :indieId")
    Optional<List<IndieGenre>> findByIndieId(Long indieId);
    @Query("SELECT g AS genre, p.poster AS poster " +
            "FROM Genre g " +
            "LEFT JOIN IndieGenre ig ON g.id = ig.genre.id " +
            "LEFT JOIN IndieMovie m ON ig.indieMovie.id = m.id " +
            "LEFT JOIN Poster p ON m.id = p.indieMovie.id " +
            "WHERE g.id IN :genreIds " +
            "GROUP BY g.id")
    List<Object[]> findFirstPosterByGenreIds(@Param("genreIds") List<Long> genreIds);

    @Query("SELECT ig FROM IndieGenre ig WHERE ig.genre.id = :genreId")
    Optional<List<IndieGenre>> findByGenreId(Long genreId);


}
