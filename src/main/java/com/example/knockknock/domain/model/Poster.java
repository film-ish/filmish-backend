package com.example.knockknock.domain.model;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Poster {
    private Long id;
    private String poster;

    @Column(nullable = false)
    private IndieMovie indieId;
}
