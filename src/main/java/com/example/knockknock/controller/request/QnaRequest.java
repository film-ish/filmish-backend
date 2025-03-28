package com.example.knockknock.controller.request;

import lombok.Getter;
import lombok.Setter;

public class QnaRequest {
    @Getter
    @Setter
    public static class WriteQna {
        private Long makerId;
        private String title;
        private String content;
    }
}
