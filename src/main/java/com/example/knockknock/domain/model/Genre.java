package com.example.knockknock.domain.model;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Genre {
    private Long id;

    @Column(unique = true, length = 100, nullable = false)
    private String name;

    private String image;
}
