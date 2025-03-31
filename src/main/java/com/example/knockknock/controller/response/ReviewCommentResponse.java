package com.example.knockknock.controller.response;

import com.example.knockknock.entity.MakerMovie;
import com.example.knockknock.entity.ReviewComment;
import com.example.knockknock.entity.User;
import lombok.*;

import java.util.List;

public class ReviewCommentResponse {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Detail{
        private Long id;
        private String writer;
        private String writerImage;
        private String writerType;
        private String content;
        private List<Detail> comments;

        public static Detail of(ReviewComment reviewComment,
                                                      User writer, MakerMovie makerMovie,
                                                      List<Detail> comments){
            return Detail.builder()
                    .id(reviewComment.getId())
                    .writer(writer.getNickname())
                    .writerImage(writer.getHeadImage())
                    .writerType(makerMovie != null ? makerMovie.getType().toString() : null)
                    .content(reviewComment.getContent())
                    .comments(comments)
                    .build();
        }
    }
}
