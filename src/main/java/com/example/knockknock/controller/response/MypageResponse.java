package com.example.knockknock.controller.response;

import com.example.knockknock.entity.IndieMovie;
import com.example.knockknock.entity.Rate;
import com.example.knockknock.entity.Review;
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
}
