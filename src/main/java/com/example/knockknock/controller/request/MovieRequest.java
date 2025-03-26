package com.example.knockknock.controller.request;

import lombok.Getter;
import lombok.Setter;

public class MovieRequest {
    @Getter
    @Setter
    public static class LikeIndieRequest{
        private Long indieId;
    }
}
