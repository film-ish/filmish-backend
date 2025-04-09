package com.example.knockknock.repository;

import com.example.knockknock.entity.Stillcut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StillcutRepository extends JpaRepository<Stillcut, Long> {

    @Query("SELECT s FROM Stillcut s WHERE s.indieMovie.id = :indieId")
    Optional<List<Stillcut>> findByIndieId(Long indieId);

    @Query("SELECT s.indieMovie.id AS movieId, s.stillcut AS stillcutUrl " +
            "FROM Stillcut s " +
            "WHERE s.id = (SELECT MIN(s2.id) FROM Stillcut s2 WHERE s2.indieMovie.id = s.indieMovie.id) " +
            "AND s.indieMovie.id IN :movieIds")
    List<Object[]> findFirstStillcutByMovieIds(@Param("movieIds") List<Long> movieIds);

}
