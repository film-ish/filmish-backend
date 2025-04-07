package com.example.knockknock.repository;

import com.example.knockknock.entity.LikeCommercial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LikeCommercialRepository extends JpaRepository<LikeCommercial, Long> {
    @Query("SELECT lc FROM LikeCommercial lc WHERE lc.user.id = :userId")
    Optional<List<LikeCommercial>> findByUserId(Long userId);
}
