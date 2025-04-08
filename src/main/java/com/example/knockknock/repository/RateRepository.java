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

    @Query(value = "SELECT r FROM Rate r WHERE r.user.id = :userId",
            countQuery = "SELECT COUNT(r) FROM Rate r WHERE r.user.id = :userId")
    Page<Rate> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT r FROM Rate r WHERE r.indieMovie.id = :indieId")
    Optional<List<Rate>> findAllByIndieId(Long indieId);

}
