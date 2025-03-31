package com.example.knockknock.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ReviewRequest {
    @Getter
    public static class Create {
        private Long indieId;
        private String title;
        private String content;
        private List<MultipartFile> images;
    }

    @Getter
    @NoArgsConstructor
    public static class Update {
        private String title;
        private String content;
    }

    @Getter
    public static class CreateComment {
        private Long reviewId;
        private String content;
        private Long parentId;
    }

    @Getter
    public static class UpdateComment{
        private String content;
    }
}
