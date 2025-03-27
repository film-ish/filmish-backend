package com.example.knockknock.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ReviewRequest {
    @Getter
    @Setter
    public static class Write {
        private Long indieId;
        private String title;
        private String content;
        private List<MultipartFile> images;
    }

    @Getter
    @NoArgsConstructor
    public static class Modify{
        private String title;
        private String content;
    }
}
