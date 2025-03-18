package com.example.knockknock.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 *
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Review extends BaseTimeEntity {
    @Id
    @Column(name="id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="title", nullable = false)
    private String title;

    @Column(name="content", nullable = false)
    private String content;

    @Column(name="views", nullable = false)
    @ColumnDefault("0")
    private int views;

    @Column(name="deleted_at", columnDefinition = "TIMESTAMP")
    private Instant deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @Builder
    public Review(String title, String content, int views, Instant deletedAt, User user) {
        this.title = title;
        this.content = content;
        this.views = views;
        this.deletedAt = deletedAt;
        this.user = user;
    }
}
