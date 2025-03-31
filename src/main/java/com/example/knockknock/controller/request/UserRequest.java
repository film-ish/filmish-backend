package com.example.knockknock.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import org.springframework.web.multipart.MultipartFile;

public class UserRequest {
    @Getter
    public static class Join {
        private String email;
        private String password;
        private String nickname;
        private Date birth;
        private String image;
        private String phone;
    }

    @Getter
    public static class Login {
        private String email;
        private String password;
    }

    @Getter
    public static class Modify {
        private String nickname;
        private MultipartFile image;
    }

    @Getter
    public static class ModifyPassword {
        private String newPassword;
    }
}
