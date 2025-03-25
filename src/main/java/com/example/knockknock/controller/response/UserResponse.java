package com.example.knockknock.controller.response;

import com.example.knockknock.entity.User;
import lombok.*;

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
                    .headImage(user.getHead_image())
                    .build();
        }
    }
}
