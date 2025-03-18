package com.example.knockknock.domain.model;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Keyword {
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private IndieMovie indieId;
}
