package com.example.knockknock.repository;

import com.example.knockknock.entity.QnaComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QnaCommentRepository extends JpaRepository<QnaComment, Long> {
    @Query("SELECT c FROM QnaComment c WHERE c.qna.id = :qnaId")
    Optional<List<QnaComment>> findByQnaId(Long qnaId);

    @Query("SELECT c FROM QnaComment c WHERE c.parentComment.id = :parentId")
    Optional<List<QnaComment>> findByParentCommentId(Long parentId);
}
