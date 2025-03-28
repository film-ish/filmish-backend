package com.example.knockknock.controller.response;

import com.example.knockknock.entity.IndieMovie;
import com.example.knockknock.entity.Maker;
import com.example.knockknock.entity.Type;
import lombok.*;

import java.util.Date;
import java.util.List;

@Data
public class MakerResponse {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Role{
        private Long id;
        private String name;
        private Type type;
        private String thumbnailImage;

        public static MakerResponse.Role of(Maker maker, Type type){
            return Role.builder()
                    .id(maker.getId())
                    .name(maker.getName())
                    .type(type)
                    .thumbnailImage(maker.getThumbnailImage())
                    .build();
        }
    }
}
