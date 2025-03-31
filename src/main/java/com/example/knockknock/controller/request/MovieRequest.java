package com.example.knockknock.controller.request;

import lombok.Getter;

public class MovieRequest {
    @Getter
    public static class LikeIndie {
        private Long indieId;
    }
}
