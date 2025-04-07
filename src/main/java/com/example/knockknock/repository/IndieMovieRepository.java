package com.example.knockknock.repository;

import com.example.knockknock.entity.IndieGenre;
import com.example.knockknock.entity.IndieMovie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IndieMovieRepository extends JpaRepository<IndieMovie, Long> {
    @Query("SELECT im FROM IndieMovie im WHERE im.title LIKE %:title%")
    List<IndieMovie> findByTitle(@Param("title") String title);

    @Query("SELECT im FROM MakerMovie mm JOIN mm.indieMovie im WHERE mm.maker.id = :makerId")
    List<IndieMovie> findByMakerId(@Param("makerId") Long makerId);

    @Query(value = "SELECT im FROM IndieGenre ig JOIN ig.indieMovie im WHERE ig.genre.id = :genreId",
            countQuery = "SELECT COUNT(ig) FROM IndieGenre ig WHERE ig.genre.id = :genreId")
    Page<IndieMovie> findByGenreId(@Param("genreId") Long genreId, Pageable pageable);
}
