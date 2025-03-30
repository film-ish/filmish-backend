package com.example.knockknock.controller.request;

import lombok.Getter;
import lombok.Setter;

public class RateRequest {
    @Getter
    @Setter
    public static class WriteRate {
        private Long indieId;
        private float value;
        private String content;
    }
}
