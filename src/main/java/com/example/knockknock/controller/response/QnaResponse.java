package com.example.knockknock.controller.response;

import com.example.knockknock.entity.Qna;
import com.example.knockknock.entity.User;
import lombok.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;

public class QnaResponse {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Detail{
        private Long qnaId;
        private String title;
        private String writer;
        private String writerImage;
        private Instant createdAt;
        private Instant updatedAt;
        private String content;
        private List<QnaCommentResponse.Detail> comments;

        public static Detail of(Qna qna, User writer, List<QnaCommentResponse.Detail> comments){
            return Detail.builder()
                    .qnaId(qna.getId())
                    .title(qna.getTitle())
                    .writer(writer.getNickname())
                    .writerImage(writer.getHeadImage())
                    .createdAt(qna.getCreatedAt())
                    .updatedAt(qna.getUpdatedAt())
                    .content(qna.getContent())
                    .comments(comments)
                    .build();

        }
    }
}
