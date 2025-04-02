package com.example.knockknock.controller.response;

import com.example.knockknock.entity.IndieMovie;
import com.example.knockknock.entity.Review;
import lombok.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class IndieResponse {
    @Getter
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

    @Getter
    @Builder
    public static class LikeDetail {
        private Long id;
        private String title;
        private String poster;
        private Date pubDate;
        private int runningTime;
        private float average;
        private List<String> genres;

        public static LikeDetail of(IndieMovie movie, String poster,
                                    float average, List<String> genres){
            return LikeDetail.builder()
                    .id(movie.getId())
                    .title(movie.getTitle())
                    .poster(poster)
                    .pubDate(movie.getPubdate())
                    .runningTime(movie.getRunningTime())
                    .average(average)
                    .genres(genres)
                    .build();
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Approximate {
        private Long id;
        private String title;
        private float average;
        private String stillcut;

        public static Approximate of(IndieMovie movie, float average, String stillcut){
            return Approximate.builder()
                    .id(movie.getId())
                    .title(movie.getTitle())
                    .average(average)
                    .stillcut(stillcut)
                    .build();
        }
    }
}
