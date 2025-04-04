package com.example.knockknock.controller.response;

import com.example.knockknock.entity.CommercialMovie;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Data
public class CommercialResponse {
    @Getter
    @Builder
    public static class Detail {
        private Long id;
        private String title;
        private String poster;
        private Date pubDate;
        private List<String> categories;

        public static Detail of(CommercialMovie movie, List<String> categories){
            return Detail.builder()
                    .id(movie.getId())
                    .title(movie.getTitle())
                    .poster(movie.getPoster())
                    .pubDate(movie.getPubdate())
                    .categories(categories)
                    .build();
        }
    }
}
