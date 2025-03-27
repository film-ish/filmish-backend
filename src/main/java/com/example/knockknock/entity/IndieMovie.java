package com.example.knockknock.entity;

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

    @Column(length = 50, nullable = false)
    private String type;
}
