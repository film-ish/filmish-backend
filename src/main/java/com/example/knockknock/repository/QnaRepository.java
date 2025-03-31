package com.example.knockknock.repository;

import com.example.knockknock.entity.Qna;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QnaRepository extends JpaRepository<Qna, Long> {
    @Query(value = "SELECT q FROM Qna q WHERE q.maker.id = :makerId",
            countQuery = "SELECT COUNT(q) FROM Qna q WHERE q.maker.id = :makerId")
    Page<Qna> findByMakerId(@Param("makerId") Long makerId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM Qna q WHERE q.id = :id")
    Optional<Qna> findByIdWithLock(@Param("id") Long id);
}
