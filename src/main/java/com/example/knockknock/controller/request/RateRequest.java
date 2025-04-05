package com.example.knockknock.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class RateRequest {
    @Getter
    @Setter
    @Schema(name="RateRequest.Create", description = "평점 등록 API")
    public static class Create {
        private Long indieId;
        private float value;
        private String content;
    }

    @Getter
    @NoArgsConstructor
    @Schema(name="RateRequest.Update", description = "평점 수정 API")
    public static class Update {
        private float value;
        private String content;
    }
}
