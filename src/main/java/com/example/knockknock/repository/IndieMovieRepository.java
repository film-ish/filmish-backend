package com.example.knockknock.repository;

import com.example.knockknock.entity.IndieMovie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndieMovieRepository extends JpaRepository<IndieMovie, Long> {
}
