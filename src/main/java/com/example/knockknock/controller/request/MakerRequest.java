package com.example.knockknock.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MakerRequest {

    @Getter
    @NoArgsConstructor
    @Schema(name="MakerRequest.Update", description = "영화인 수정 API")
    public static class Update {
        private String name;
        private String image;
    }
}
