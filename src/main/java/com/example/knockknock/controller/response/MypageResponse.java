package com.example.knockknock.controller.response;

import com.example.knockknock.entity.*;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class MypageResponse {
    @Getter
    @Builder
    public static class RateDetail {
        private Long rateId;
        private String content;
        private float value;
        private Instant createdAt;
        private Instant updatedAt;
        private Long movieId;
        private String title;
        private String poster;

        public static RateDetail of(Rate rate, IndieMovie indieMovie, String poster){
            return RateDetail.builder()
                    .rateId(rate.getId())
                    .content(rate.getContent())
                    .value(rate.getValue())
                    .createdAt(rate.getCreatedAt())
                    .updatedAt(rate.getUpdatedAt())
                    .movieId(indieMovie.getId())
                    .title(indieMovie.getTitle())
                    .poster(poster)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ReviewDetail {
        private Long reviewId;
        private String title;
        private String content;
        private List<String> image;
        private Instant createdAt;
        private Instant updatedAt;
        private Integer views;
        private Long movieId;
        private String movieTitle;

        public static ReviewDetail of(Review review, IndieMovie indieMovie, List<String> image){
            return ReviewDetail.builder()
                    .reviewId(review.getId())
                    .title(review.getTitle())
                    .content(review.getContent())
                    .image(image)
                    .createdAt(review.getCreatedAt())
                    .updatedAt(review.getUpdatedAt())
                    .views(review.getViews())
                    .movieId(indieMovie.getId())
                    .movieTitle(indieMovie.getTitle())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class QnaDetail {
        private Long qnaId;
        private String title;
        private String content;
        private Instant createdAt;
        private Instant updatedAt;
        private String writerName;
        private String writerImage;
        private Long makerId;
        private String makerName;
        private List<QnaCommentResponse.Detail> comments;

        public static QnaDetail of(Qna qna, Maker maker, List<QnaCommentResponse.Detail> comments){
            return QnaDetail.builder()
                    .qnaId(qna.getId())
                    .title(qna.getTitle())
                    .content(qna.getContent())
                    .createdAt(qna.getCreatedAt())
                    .updatedAt(qna.getUpdatedAt())
                    .writerName(qna.getUser().getNickname())
                    .writerName(qna.getUser().getHeadImage())
                    .makerId(maker.getId())
                    .makerName(maker.getName())
                    .comments(comments)
                    .build();
        }
    }
}
