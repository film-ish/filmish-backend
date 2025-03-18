package com.example.knockknock.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CommercialGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id")
    @Column(name = "genre_id", nullable = false)
    private Genre genreId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indie_id")
    @Column(name = "user_id", nullable = false)
    private IndieMovie indieId;
}
