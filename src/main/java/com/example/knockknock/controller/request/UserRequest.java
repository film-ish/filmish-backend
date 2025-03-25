package com.example.knockknock.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class UserRequest {
    @Getter
    @Setter
    public static class JoinRequest {
        private String email;
        private String password;
        private String nickname;
        private Date birth;
        private String image;
        private String phone;
    }

    @Getter
    @Setter
    public static class LoginRequest {
        private String email;
        private String password;
    }

}
