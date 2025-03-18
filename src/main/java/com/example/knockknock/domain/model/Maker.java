package com.example.knockknock.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Setter
public class Maker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    private String image;

    @ColumnDefault("0")
    @Column(name = "total_Contents")
    private int totalContents;

    @Column(nullable = false)
    private Type type;
}
