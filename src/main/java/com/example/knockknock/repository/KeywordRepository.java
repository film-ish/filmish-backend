package com.example.knockknock.repository;

import com.example.knockknock.entity.IndieMovie;
import com.example.knockknock.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    @Query("SELECT k.indieMovie FROM Keyword k WHERE k.name = :name")
    Optional<List<IndieMovie>> findByName(String name);
}
