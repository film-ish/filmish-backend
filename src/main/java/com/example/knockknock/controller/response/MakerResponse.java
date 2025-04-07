package com.example.knockknock.controller.response;

import com.example.knockknock.document.MakerDocument;
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
    public static class ListItem {
        private Long actorId;
        private String name;
        private String email;
        private String role;
        private String image;
        private Long qnaCnt;
        private Long indieCnt;
        private String movieTitle;

        public static MakerResponse.ListItem of(Maker maker, String email, String role,
                                              Long qnaCnt, Long indieCnt, String movieTitle) {
            return ListItem.builder()
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

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Filmography {
        private Long movieId;
        private String movieName;
        private String pubDate;

        public static Filmography of(IndieMovie movie) {
            String pubDateStr = movie.getPubdate() != null
                    ? movie.getPubdate().toString()
                    : null;

            return Filmography.builder()
                    .movieId(movie.getId())
                    .movieName(movie.getTitle())
                    .pubDate(pubDateStr)
                    .build();
        }
    }
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Detail {
        private Long id;
        private Long userId;
        private String name;
        private String image;
        private Long qnaCount;
        private List<Filmography> filmography;

        public static Detail of(Maker maker, Long userId, Long qnaCount, List<Filmography> filmography) {
            return Detail.builder()
                    .id(maker.getId())
                    .userId(userId)
                    .name(maker.getName())
                    .image(maker.getThumbnailImage())
                    .qnaCount(qnaCount)
                    .filmography(filmography)
                    .build();
        }

        public static Detail of(MakerDocument maker, Long userId, String thumbnailImage, Long qnaCount, List<Filmography> filmography) {
            return Detail.builder()
                    .id(Long.parseLong(maker.getId()))
                    .userId(userId)
                    .name(maker.getName())
                    .image(thumbnailImage)
                    .qnaCount(qnaCount)
                    .filmography(filmography)
                    .build();
        }
    }

}
