package com.example.knockknock.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Poster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String poster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indie_id")
    @Column(name = "indie_movie_id", nullable = false)
    private IndieMovie indieMovie;
}
