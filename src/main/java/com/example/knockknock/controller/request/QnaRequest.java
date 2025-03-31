package com.example.knockknock.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class QnaRequest {
    @Getter
    @Setter
    public static class Create {
        private Long makerId;
        private String title;
        private String content;
    }

    @Getter
    @Setter
    public static class Update{
        private String title;
        private String content;
    }

    @Getter
    @Setter
    public static class CreateComment {
        private String content;
        private Long parentId;
    }

    @Getter
    @Setter
    public static class UpdateComment {
        private String content;
    }
}
