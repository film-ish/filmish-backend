package com.example.knockknock.repository;

import com.example.knockknock.entity.CommercialGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface CommercialGenreRepository extends JpaRepository<CommercialGenre, Long> {
    @Query("SELECT cg FROM CommercialGenre cg WHERE cg.commercialMovie.id IN :commercialIds")
    List<CommercialGenre> findByCommercialIdIn(Set<Long> commercialIds);
}
