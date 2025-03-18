package com.example.knockknock.domain.model;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Stillcut {
    private Long id;

    @Column(nullable = false)
    private String image;

    @Column(nullable = false)
    private IndieMovie indieId;
}
