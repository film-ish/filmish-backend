package com.example.knockknock.controller.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class MovieRequest {
    @Getter
    @Setter
    public static class LikeIndie {
        private Long indieId;
    }
}
