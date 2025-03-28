package com.example.knockknock.repository;

import com.example.knockknock.entity.Stillcut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StillcutRepository extends JpaRepository<Stillcut, Long> {

    @Query("SELECT s FROM Stillcut s WHERE s.indieMovie.id = :indieId")
    Optional<List<Stillcut>> findByIndieId(Long indieId);
}
