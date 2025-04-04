package com.example.knockknock.controller.request;

import lombok.Getter;

import java.util.List;

public class MovieRequest {
    @Getter
    public static class LikeIndie {
        private Long indieId;
    }

    @Getter
    public static class LikeCommercial {
        private List<Long> commercialId;
    }
}
