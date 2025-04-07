package com.example.knockknock.controller.response;

import com.example.knockknock.entity.User;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Data
public class UserResponse {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LoginResponse{
        private Long id;
        private String email;
        private String nickname;
        private String headImage;

        public static LoginResponse of(User user){
            return LoginResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .headImage(user.getHeadImage())
                    .build();
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UserInfoResponse{
        private Long id;
        private String nickname;
        private String image;
        private LocalDate birth;
        private String email;

        public static UserInfoResponse of(User user){
            return UserInfoResponse.builder()
                    .id(user.getId())
                    .nickname(user.getNickname())
                    .image(user.getImage())
                    .birth(user.getBirth())
                    .email(user.getEmail())
                    .build();
        }
    }


}
