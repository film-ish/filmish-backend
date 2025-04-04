package com.example.knockknock.repository;

import com.example.knockknock.entity.QnaComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QnaCommentRepository extends JpaRepository<QnaComment, Long> {
    @Query("SELECT c FROM QnaComment c WHERE c.qna.id = :qnaId")
    Optional<List<QnaComment>> findByQnaId(Long qnaId);

    @Query("SELECT c FROM QnaComment c WHERE c.parentComment.id = :parentId")
    Optional<List<QnaComment>> findByParentCommentId(Long parentId);

    @Query(value = "SELECT qc FROM QnaComment qc WHERE qc.user.id = :userId",
             countQuery = "SELECT COUNT(qc) FROM QnaComment qc WHERE qc.user.id = :userId")
    Page<QnaComment> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT qc FROM QnaComment qc WHERE qc.user.id = :userId")
    Optional<List<QnaComment>> findByUserId(Long userId);

    @Query("SELECT DISTINCT c FROM QnaComment c JOIN Qna q ON c.qna.id = q.id WHERE c.qna.id = :qnaId")
    List<QnaComment> findCommentsWithSubComments(@Param("qnaId") Long qnaId);


}
