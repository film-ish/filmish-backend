package com.example.knockknock.controller.response;

import com.example.knockknock.entity.IndieMovie;
import com.example.knockknock.entity.Rate;
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
}
