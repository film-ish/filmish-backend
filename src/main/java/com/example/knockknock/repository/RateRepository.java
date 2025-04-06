package com.example.knockknock.repository;

import com.example.knockknock.entity.Rate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface RateRepository extends JpaRepository<Rate, Long> {
    @Query(value = "SELECT r FROM Rate r WHERE r.indieMovie.id = :indieId",
            countQuery = "SELECT COUNT(r) FROM Rate r WHERE r.indieMovie.id = :indieId")
    Page<Rate> findByIndieId(@Param("indieId") Long indieId, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT m.indieMovie.id) FROM MakerMovie m WHERE m.maker.id = :makerId")
    Long countDistinctMoviesByMakerId(@Param("makerId") Long makerId);

    @Query(value = "SELECT r FROM Review r WHERE r.user.id = :userId",
            countQuery = "SELECT COUNT(r) FROM Review r WHERE r.user.id = :userId")
    Page<Rate> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT r FROM Rate r WHERE r.indieMovie.id = :indieId")
    Optional<List<Rate>> findAllByIndieId(Long indieId);

//    @Query("SELECT r.indieMovie.id AS movieId, AVG(r.value) AS averageRating " +
//            "FROM Rate r " +
//            "GROUP BY r.indieMovie.id " +
//            "HAVING AVG(r.value) BETWEEN :minValue AND :maxValue " +
//            "ORDER BY " +
//            "    CASE WHEN r.indieMovie.id IN :recommendedMovieIds THEN 0 ELSE 1 END, " +
//            "    AVG(r.value) DESC")
//    Page<Object[]> findMoviesWithAverageRatingAndRecommendation(
//            @Param("minValue") double minValue,
//            @Param("maxValue") double maxValue,
//            @Param("recommendedMovieIds") List<Long> recommendedMovieIds,
//            Pageable pageable
//    );

    @Query("SELECT AVG(r.value) FROM Rate r WHERE r.indieMovie.id = :indieId AND r.deletedAt IS NULL")
    Double findAverageRatingByIndieMovieId(@Param("indieId") Long indieId);


    @Query("""
    SELECT m.id, 
           AVG(r.value), 
           COUNT(r.id) 
    FROM Rate r 
    JOIN r.indieMovie m 
    WHERE r.value BETWEEN :minValue AND :maxValue 
    GROUP BY m.id
    ORDER BY AVG(r.value) DESC
""")
    Page<Object[]> findMoviesWithAverageRatingAndRecommendation(
            @Param("minValue") double minValue,
            @Param("maxValue") double maxValue,
            @Param("recommendedIds") List<Long> recommendedIds,
            Pageable pageable
    );
}


