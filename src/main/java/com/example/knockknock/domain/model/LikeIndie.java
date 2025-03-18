package com.example.knockknock.domain.model;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class LikeIndie {
    private Long id;

    @Column(nullable = false)
    private IndieMovie indieId;

    @Column(nullable = false)
    private User userId;
}
