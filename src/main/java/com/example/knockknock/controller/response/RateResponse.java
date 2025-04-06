package com.example.knockknock.controller.response;

import com.example.knockknock.entity.IndieMovie;
import com.example.knockknock.entity.Poster;
import com.example.knockknock.entity.Rate;
import lombok.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Data
public class RateResponse {
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Detail {
        private Long id;
        private String writerName;
        private String writerImage;
        private float value;
        private String content;
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
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MovieListByRating {
        private Long movieId;
        private String title;
        private String posterUrl;
        private float averageRating;
        private int ratingCount;
        private String genre;
        private Date pubdate;

        public static RateResponse.MovieListByRating of(IndieMovie movie, float averageRating, int ratingCount, String posterUrl, String genres) {
            return MovieListByRating.builder()
                    .movieId(movie.getId())
                    .title(movie.getTitle())
                    .posterUrl(posterUrl)
                    .averageRating(averageRating)
                    .ratingCount(ratingCount)
                    .genre(genres)
                    .pubdate(movie.getPubdate())
                    .build();
        }
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MovieRatingPage {
        private List<MovieListByRating> movies;
        private int currentPage;
        private int totalPages;
        private long totalElements;
        private double minRating;
        private double maxRating;

        public static RateResponse.MovieRatingPage of(
                List<MovieListByRating> movies,
                int currentPage,
                int totalPages,
                long totalElements,
                double minRating,
                double maxRating) {
            return MovieRatingPage.builder()
                    .movies(movies)
                    .currentPage(currentPage)
                    .totalPages(totalPages)
                    .totalElements(totalElements)
                    .minRating(minRating)
                    .maxRating(maxRating)
                    .build();
        }
    }
}
