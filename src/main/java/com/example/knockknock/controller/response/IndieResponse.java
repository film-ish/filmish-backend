package com.example.knockknock.controller.response;

import com.example.knockknock.document.MovieDocument;
import com.example.knockknock.entity.IndieMovie;
import lombok.*;

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
    @Setter
    @Builder
    public static class LikeDetail<T> {
        private Long id;
        private String title;
        private String poster;
        private Date pubDate;
        private int runningTime;
        private T value;
        private List<String> genres;

        @Builder.Default
        private Boolean like = false;

        public static <T> LikeDetail<T> of(IndieMovie movie, String poster,
                                           T value, List<String> genres){
            return LikeDetail.<T>builder()
                    .id(movie.getId())
                    .title(movie.getTitle())
                    .poster(poster)
                    .pubDate(movie.getPubdate())
                    .runningTime(movie.getRunningTime())
                    .value(value)
                    .genres(genres)
                    .build();
        }

        public static <T> LikeDetail<T> of(IndieMovie movie, String poster,
                                    T value, List<String> genres, Boolean like){
            return LikeDetail.<T>builder()
                    .id(movie.getId())
                    .title(movie.getTitle())
                    .poster(poster)
                    .pubDate(movie.getPubdate())
                    .runningTime(movie.getRunningTime())
                    .value(value)
                    .genres(genres)
                    .like(like!= null ? like : false)
                    .build();
        }

        public static <T> LikeDetail<T> of(MovieDocument movie, String poster,
                                           T value, List<String> genres){
            return LikeDetail.<T>builder()
                    .id(Long.parseLong(movie.getId()))
                    .title(movie.getTitle())
                    .poster(poster)
                    .pubDate(movie.getPubDate())
                    .runningTime(movie.getRunningTime())
                    .value(value)
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
        private Boolean like;

        public static Approximate of(IndieMovie movie, float average, String stillcut, boolean like){
            return Approximate.builder()
                    .id(movie.getId())
                    .title(movie.getTitle())
                    .average(average)
                    .stillcut(stillcut)
                    .like(like)
                    .build();
        }
    }
}
