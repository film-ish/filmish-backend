package com.example.knockknock.controller.response;

import com.example.knockknock.entity.Rate;
import lombok.*;

import java.time.Instant;

@Data
public class RateResponse {
    @Builder
    public static class Detail {
        private Long id;
        private String writerName;
        private String writerImage;
        private float value;
        private Instant createdAt;
        private Instant updatedAt;

        public static RateResponse.Detail of(Rate rate, String writer, String writerImage){
            return Detail.builder()
                    .id(rate.getId())
                    .writerName(writer)
                    .writerImage(writerImage)
                    .value(rate.getValue())
                    .createdAt(rate.getCreatedAt())
                    .updatedAt(rate.getUpdatedAt())
                    .build();
        }
    }
}
