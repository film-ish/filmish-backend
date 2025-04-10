package com.example.knockknock.controller.response;

import com.example.knockknock.entity.Review;
import lombok.*;

import java.time.Instant;
import java.util.List;

public class ReviewResponse {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Detail {
        private Long id;
        private Long indieId;
        private String title;
        private String content;
        private String writerName;
        private String writerImage;
        private Instant createdAt;
        private Instant updatedAt;
        private int views;
        private List<ReviewImageResponse.Detail> images;

        public static ReviewResponse.Detail of(Review review, String writer, String writerImage, List<ReviewImageResponse.Detail> images){
            return Detail.builder()
                    .id(review.getId())
                    .indieId(review.getIndieMovie().getId())
                    .title(review.getTitle())
                    .content(review.getContent())
                    .writerName(writer)
                    .writerImage(writerImage)
                    .createdAt(review.getCreatedAt())
                    .updatedAt(review.getUpdatedAt())
                    .views(review.getViews())
                    .images(images)
                    .build();
        }
    }
}
