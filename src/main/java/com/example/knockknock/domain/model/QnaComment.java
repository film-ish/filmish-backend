package com.example.knockknock.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class QnaComment extends BaseTimeEntity {
    @Id
    @Column(name="id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="content", nullable = false)
    private String content;

    @Column(name="deleted_at", columnDefinition = "TIMESTAMP")
    private Instant deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="qna_id", nullable = false)
    private Qna qna;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="parent_comment_id")
    private QnaComment parentComment;

    @OneToMany(mappedBy = "parentComment")
    private List<QnaComment> childComments = new ArrayList<>();

    @Builder
    public QnaComment(String content, Instant deletedAt, User user, Qna qna, QnaComment parentComment) {
        this.content = content;
        this.deletedAt = deletedAt;
        this.user = user;
        this.qna = qna;
        this.parentComment = parentComment;
    }

    // 부모 댓글 설정 메서드
    public void setParentComment(QnaComment parentComment) {
        this.parentComment = parentComment;
        if (parentComment != null) {
            parentComment.getChildComments().add(this);
        }
    }
}
