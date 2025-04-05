package com.example.knockknock.controller.response;

import com.example.knockknock.entity.QnaComment;
import com.example.knockknock.entity.User;
import lombok.*;

import java.util.List;

public class QnaCommentResponse {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Detail{
        private Long id;
        private String writer;
        private String writerImage;
        private String content;
        private List<Detail> subComments;

        public static Detail of(QnaComment qnaComment,
                                User writer, List<Detail> comments){
            return Detail.builder()
                    .id(qnaComment.getId())
                    .writer(writer.getNickname())
                    .writerImage(writer.getHeadImage())
                    .content(qnaComment.getContent())
                    .subComments(comments)
                    .build();
        }
    }
}
