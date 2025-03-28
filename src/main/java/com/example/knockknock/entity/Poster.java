package com.example.knockknock.entity;

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
    private String thumbnail;       // 썸네일 이미지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indie_id", nullable = false)
    private IndieMovie indieMovie;
}
