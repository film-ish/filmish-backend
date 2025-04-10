package com.example.knockknock.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.Date;
import java.util.List;

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

    private Date pubdate;

    @Column(name = "running_time")
    private int runningTime;

    @ColumnDefault("0")
    @Column(name = "average_rating")
    private float averageRating;

    @Column(length = 50, nullable = false)
    private String type;

    @OneToMany(mappedBy = "indieMovie", fetch = FetchType.LAZY)
    private List<Poster> posters;

    @OneToMany(mappedBy = "indieMovie", fetch = FetchType.LAZY)
    private List<Stillcut> stillcuts;

    @OneToMany(mappedBy = "indieMovie", fetch = FetchType.LAZY)
    private List<IndieGenre> genres;
}
