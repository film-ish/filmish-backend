package com.example.knockknock.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Rate extends BaseTimeEntity {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "value", nullable = false)
    private int value;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "deleted_at", columnDefinition = "TIMESTAMP")
    private Instant deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indie_id", nullable = false)
    private IndieMovie indieMovie;

    @Builder
    public Rate(int value, String content, Instant deletedAt, User user, IndieMovie indieMovie) {
        this.value = value;
        this.content = content;
        this.deletedAt = deletedAt;
        this.user = user;
        this.indieMovie = indieMovie;
    }
}