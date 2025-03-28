package com.example.knockknock.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "like_commercial",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_user_commercial",
                        columnNames = {"user_id", "commercial_id"}
                )
        }
)
public class LikeCommercial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commercial_id", nullable = false)
    private CommercialMovie commercialMovie;
}
