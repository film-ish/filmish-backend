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

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Detail {
        private Long actorId;
        private String name;
        private String email;
        private String role;
        private String image;
        private Long qnaCnt;
        private Long indieCnt;
        private String movieTitle;

        public static MakerResponse.Detail of(Maker maker, String email, String role,
                                              Long qnaCnt, Long indieCnt, String movieTitle) {
            return Detail.builder()
                    .actorId(maker.getId())
                    .name(maker.getName())
                    .email(email)
                    .role(role)
                    .image(maker.getThumbnailImage())
                    .qnaCnt(qnaCnt)
                    .indieCnt(indieCnt)
                    .movieTitle(movieTitle)
                    .build();
        }
    }
}
