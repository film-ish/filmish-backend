package com.example.knockknock.controller.request;

import lombok.Getter;

import java.time.LocalDate;

import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

public class UserRequest {
    @Getter
    @Setter
    public static class Join {
        private String email;
        private String password;
        private String nickname;
        private LocalDate birth;
        private String image;
    }

    @Getter
    @Setter
    public static class Login {
        private String email;
        private String password;
    }

    @Getter
    @Setter
    public static class Modify {
        private String nickname;
        private MultipartFile image;
    }

    @Getter
    @Setter
    public static class ModifyPassword {
        private String newPassword;
    }
}
