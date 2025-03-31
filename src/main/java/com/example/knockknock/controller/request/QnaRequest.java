package com.example.knockknock.controller.request;

import lombok.Getter;

public class QnaRequest {
    @Getter
    public static class Create {
        private Long makerId;
        private String title;
        private String content;
    }

    @Getter
    public static class Update{
        private String title;
        private String content;
    }

    @Getter
    public static class CreateComment {
        private String content;
        private Long parentId;
    }

    @Getter
    public static class UpdateComment {
        private String content;
    }
}
