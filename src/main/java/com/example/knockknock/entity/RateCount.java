package com.example.knockknock.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RateCount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ColumnDefault("0")
    private int count;
    private float rate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indie_id", nullable = false)
    private IndieMovie indieMovie;
}
