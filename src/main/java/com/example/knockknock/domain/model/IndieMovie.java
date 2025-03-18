package com.example.knockknock.domain.model;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class IndieMovie {
    private Long id;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String plot;

    private LocalDate pubDate;
    private int runningTime;

    @ColumnDefault("0")
    private float averageRating;

    @ColumnDefault("0")
    private int audiences;

    @Column(nullable = false)
    private MovieType movieType;
}
