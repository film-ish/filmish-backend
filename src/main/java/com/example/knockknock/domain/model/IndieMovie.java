package com.example.knockknock.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.Date;

@Entity
@Getter
@Setter
public class IndieMovie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String plot;

    @Column(name = "pub_date")
    private Date pubDate;

    @Column(name = "running_time")
    private int runningTime;

    @ColumnDefault("0")
    @Column(name = "average_rating")
    private float averageRating;

    @ColumnDefault("0")
    private int audiences;

    @Column(name = "movie_type", nullable = false)
    private MovieType movieType;

    // kmdb의 'movie_id + movie_seq' 의미
    @Column(name = "kmdb_id")
    private Long kmdbId;
}
