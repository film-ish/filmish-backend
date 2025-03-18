package com.example.knockknock.domain.model;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Builder
public class Maker {
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    private String image;

    @ColumnDefault("0")
    private int totalContents;

    @Column(nullable = false)
    private Type type;
}
