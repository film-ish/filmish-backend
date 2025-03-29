package com.example.knockknock.controller.request;

import lombok.Getter;
import lombok.Setter;

public class QnaRequest {
    @Getter
    @Setter
    public static class Write {
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
    public static class WriteComment{
        private String content;
        private Long parentId;
    }

    @Getter
    @Setter
    public static class UpdateComment{
        private String content;
    }
}
