package com.example.knockknock.controller.response;

import com.example.knockknock.entity.IndieMovie;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class MovieResponse {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Detail {
        private Long id;
        private String title;
        private String plot;
        private Date pubDate;
        private int runningTime;
        private float averageRating;
        private String type;
        private String poster;
        private List<Map<Long, String>> stillcuts;
        private List<MakerResponse.Role> makers;

        public static Detail of(IndieMovie indieMovie, List<Map<Long, String>> stillcuts, List<MakerResponse.Role> makers){
            return Detail.builder()
                    .id(indieMovie.getId())
                    .title(indieMovie.getTitle())
                    .plot(indieMovie.getPlot())
                    .pubDate(indieMovie.getPubdate())
                    .runningTime(indieMovie.getRunningTime())
                    .averageRating(indieMovie.getAverageRating())
                    .type(indieMovie.getType())
                    .stillcuts(stillcuts)
                    .makers(makers)
                    .build();
        }
    }
}
