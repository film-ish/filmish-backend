package com.example.knockknock.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ReviewRequest {
    @Getter
    @Setter
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
    @Setter
    public static class CreateComment {
        private Long reviewId;
        private String content;
        private Long parentId;
    }

    @Getter
    @Setter
    public static class UpdateComment{
        private String content;
    }
}
