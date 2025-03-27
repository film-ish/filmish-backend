package com.example.knockknock.controller.response;

import com.example.knockknock.entity.IndieMovie;
import lombok.*;

import java.util.Date;

@Data
public class MovieResponse {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Info{
        private Long id;
        private String title;
        private String plot;
        private Date pubDate;
        private int runningTime;
        private float averageRating;
        private String type;

        public static MovieResponse.Info of(IndieMovie indieMovie){
            return Info.builder()
                    .id(indieMovie.getId())
                    .title(indieMovie.getTitle())
                    .plot(indieMovie.getPlot())
                    .pubDate(indieMovie.getPubDate())
                    .runningTime(indieMovie.getRunningTime())
                    .averageRating(indieMovie.getAverageRating())
                    .type(indieMovie.getType())
                    .build();
        }
    }
}
