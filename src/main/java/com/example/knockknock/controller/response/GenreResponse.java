package com.example.knockknock.controller.response;

import com.example.knockknock.entity.Genre;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
public class GenreResponse {
    @Getter
    @Builder
    public static class Detail{
        private Long id;
        private String name;
        private String image;

        public static Detail of(Genre genre, String image){
            return Detail.builder()
                    .id(genre.getId())
                    .name(genre.getName())
                    .image(image)
                    .build();
        }
    }
}
