package com.example.knockknock.controller.response;

import com.example.knockknock.entity.ReviewImage;
import lombok.*;

public class ReviewImageResponse {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Detail {
        private Long id;
        private String path;

        public static ReviewImageResponse.Detail of(ReviewImage reviewImage){
            return Detail.builder()
                    .id(reviewImage.getId())
                    .path(reviewImage.getPath())
                    .build();
        }
    }
}
