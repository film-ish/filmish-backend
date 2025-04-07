package com.example.knockknock.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDocumentRequest {
    @JsonProperty("movie_id")
    private String movieId;
    private String title;
    private String plot;
    private List<Genre> genres;
    private List<Keyword> keywords;
    private List<Maker> actors;
    private List<Maker> directors;

    @Data
    public static class Genre {
        private String id;
        private String name;
    }

    @Data
    public static class Keyword {
        private String name;
    }

    @Data
    public static class Maker {
        private String id;
        private String role;
        private String name;
    }
}
