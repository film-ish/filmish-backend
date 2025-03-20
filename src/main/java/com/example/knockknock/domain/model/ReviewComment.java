package com.example.knockknock.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SQLDelete(sql = "UPDATE qna SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class ReviewComment extends BaseTimeEntity {
    @Id
    @Column(name="id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="content", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="parent_comment_id")
    private ReviewComment parentComment;

    @OneToMany(mappedBy = "parentComment")
    private List<ReviewComment> childComments = new ArrayList<>();

    @Builder
    public ReviewComment(String content, User user, Review review, ReviewComment parentComment) {
        this.content = content;
        this.user = user;
        this.review = review;
        this.parentComment = parentComment;
    }

    // 부모 댓글 설정 메서드
    public void setParentComment(ReviewComment parentComment) {
        this.parentComment = parentComment;
        if (parentComment != null) {
            parentComment.getChildComments().add(this);
        }
    }
}
