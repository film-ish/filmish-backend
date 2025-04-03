package com.example.knockknock.repository;

import com.example.knockknock.entity.IndieMovie;
import com.example.knockknock.entity.MakerMovie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MakerMovieRepository extends JpaRepository<MakerMovie, Long> {
    @Query("SELECT m FROM MakerMovie m WHERE m.indieMovie.id = :indieId")
    Optional<List<MakerMovie>> findByIndieId(Long indieId);

    @Query(value = "SELECT m FROM MakerMovie m WHERE m.maker.id = :makerId",
            countQuery = "SELECT COUNT(m) FROM MakerMovie m WHERE m.maker.id = :makerId")
    Page<MakerMovie> findByMakerId(@Param("makerId") Long makerId, Pageable pageable);

    Long countByMakerId(Long makerId);

    @Query("SELECT m.indieMovie FROM MakerMovie m WHERE m.maker.id = :makerId ORDER BY RAND() LIMIT 1")
    Optional<IndieMovie> findRandomMovieByMakerId(@Param("makerId") Long makerId);}
