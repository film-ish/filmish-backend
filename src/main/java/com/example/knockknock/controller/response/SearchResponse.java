package com.example.knockknock.controller.response;

import com.example.knockknock.entity.IndieMovie;
import com.example.knockknock.entity.Maker;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class SearchResponse {
    @Getter
    @Builder
    public static class ListResult {
        private List<MovieDetail> movies;
        private List<MakerDetail> directors;
        private List<MakerDetail> actors;
        private List<KeyMovies> genreMovies;
        private List<KeyMovies> keywordMovies;

        public static ListResult of(List<MovieDetail> movies, List<MakerDetail> directors, List<MakerDetail> actors,
                                    List<KeyMovies> genreMovies, List<KeyMovies> keywordMovies){
            return ListResult.builder()
                    .movies(movies)
                    .directors(directors)
                    .actors(actors)
                    .genreMovies(genreMovies)
                    .keywordMovies(keywordMovies)
                    .build();
        }
    }

    @Getter
    @Setter
    @Builder
    public static class KeyMovies {
        private String name;
        private List<MovieDetail> movies;

        public static KeyMovies of(String name, List<MovieDetail> movies){
            return KeyMovies.builder()
                    .name(name)
                    .movies(movies)
                    .build();
        }
    }

    @Getter
    @Setter
    @Builder
    public static class MovieDetail {
        private String id;
        private String title;
        private String poster;
        private Date pubDate;
        private Integer runningTime;
        private Float rate;
        private List<String> genres;
        private boolean like;

        public static MovieDetail of (Map<String, Object> movie, String poster, Boolean like){
            List<String> genres = null;
            Object genresObj = movie.get("genres");

            if (genresObj instanceof List<?>) {
                genres = ((List<?>) genresObj).stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .toList();
            }

            return MovieDetail.builder()
                    .id((String) movie.get("movie_id"))
                    .title((String) movie.get("title"))
                    .poster(poster)
                    .pubDate((Date) movie.get("pubDate"))
                    .runningTime((Integer) movie.get("running_time"))
                    .genres(genres)
                    .like(like)
                    .build();
        }

        public static MovieDetail of (IndieMovie movie, String poster, List<String> genres, Boolean like){
            return MovieDetail.builder()
                    .id(movie.getId().toString())
                    .title(movie.getTitle())
                    .poster(poster)
                    .pubDate(movie.getPubdate())
                    .runningTime(movie.getRunningTime())
                    .genres(genres)
                    .like(like)
                    .build();
        }
    }

    @Getter
    @Setter
    @Builder
    public static class MakerDetail {
        private String id;
        private String name;
        private String image;
        private Long qnaNum;
        private List<String> filmography;

        public static MakerDetail of (Maker maker, Long qnaNum, List<String> filmography){
            return MakerDetail.builder()
                    .id(maker.getId().toString())
                    .name(maker.getName())
                    .image(maker.getImage())
                    .qnaNum(qnaNum)
                    .filmography(filmography)
                    .build();
        }
    }
}
