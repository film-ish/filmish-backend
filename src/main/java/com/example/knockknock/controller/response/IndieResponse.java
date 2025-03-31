package com.example.knockknock.controller.response;

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
    @Builder
    public static class LikeDetail {
        private Long id;
        private String title;
        private String poster;
        private Date pubDate;
        private List<String> categories;

        public static LikeDetail of(IndieMovie movie, String poster, List<String> categories){
            return LikeDetail.builder()
                    .id(movie.getId())
                    .title(movie.getTitle())
                    .poster(poster)
                    .pubDate(movie.getPubdate())
                    .categories(categories)
                    .build();
        }
    }
}
