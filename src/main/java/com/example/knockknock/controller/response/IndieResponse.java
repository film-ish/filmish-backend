package com.example.knockknock.controller.response;

import com.example.knockknock.document.MovieDocument;
import com.example.knockknock.entity.IndieMovie;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class IndieResponse {

    @Getter
    @Builder
    public static class DetailAll {
        private Long id;
        private String title;
        private String plot;
        private Date pubDate;
        private int runningTime;
        private float averageRating;
        private String type;
        private boolean like;
        private List<String> posters;
        private List<String> stillcuts;
        private List<MakerResponse.Role> makers;

        public static IndieResponse.DetailAll of(IndieMovie indieMovie, boolean like, List<String> stillcuts,
                                                 List<MakerResponse.Role> makers, List<String> posters) {
            return DetailAll.builder()
                    .id(indieMovie.getId())
                    .title(indieMovie.getTitle())
                    .plot(indieMovie.getPlot())
                    .pubDate(indieMovie.getPubdate())
                    .runningTime(indieMovie.getRunningTime())
                    .averageRating(indieMovie.getAverageRating())
                    .type(indieMovie.getType())
                    .like(like)
                    .posters(posters)
                    .stillcuts(stillcuts)
                    .makers(makers)
                    .build();
        }
    }

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
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Slf4j
    public static class LikeDetail<T> {
        private Long id;
        private String title;
        private String poster;
        private String stillcut;
        private Date pubDate;
        private int runningTime;
        private T value;
        private List<String> genres;

        @Builder.Default
        private Boolean like = false;

        // Float 타입용 생성자
        public static LikeDetail<Float> from(IndieMovie movie,
                                             String poster,
                                             String stillcut,
                                             List<String> genres,
                                             boolean like) {
            LikeDetail<Float> detail = new LikeDetail<>();
            detail.id = movie.getId();
            detail.title = movie.getTitle();
            detail.poster = poster;
            detail.stillcut = stillcut;
            detail.pubDate = movie.getPubdate();
            detail.runningTime = movie.getRunningTime();
            detail.value = movie.getAverageRating();
            detail.genres = genres;
            detail.like = like;
            return detail;
        }

        // Long 타입용 생성자
        public static LikeDetail<Long> from(IndieMovie movie,
                                             String poster,
                                             String stillcut,
                                             Long likeCount,
                                             List<String> genres,
                                             boolean like) {
            LikeDetail<Long> detail = new LikeDetail<>();
            detail.id = movie.getId();
            detail.title = movie.getTitle();
            detail.poster = poster;
            detail.stillcut = stillcut;
            detail.pubDate = movie.getPubdate();
            detail.runningTime = movie.getRunningTime();
            log.info("입력된 likeCount = {}", likeCount);
            detail.value = likeCount;
            detail.genres = genres;
            detail.like = like;
            return detail;
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StillcutDetail {
        private Long id;
        private String title;
        private float average;
        private String stillcut;
        private Boolean like;

        public StillcutDetail(IndieMovie movie, String stillcut, boolean like){
            this.id = movie.getId();
            this.title = movie.getTitle();
            this.average = movie.getAverageRating();
            this.stillcut = stillcut;
            this.like = like;
        }

        public static StillcutDetail of(IndieMovie movie, String stillcut, boolean like){
            return StillcutDetail.builder()
                    .id(movie.getId())
                    .title(movie.getTitle())
                    .average(movie.getAverageRating())
                    .stillcut(stillcut)
                    .like(like)
                    .build();
        }
    }
}
